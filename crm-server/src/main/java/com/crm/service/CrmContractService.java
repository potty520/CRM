package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.*;
import com.crm.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmContractService {

    private final CrmContractMapper contractMapper;
    private final CrmPaymentMapper paymentMapper;
    private final CrmInvoiceMapper invoiceMapper;
    private final CrmCustomerMapper customerMapper;
    private final SysUserMapper userMapper;

    public CrmContractService(CrmContractMapper contractMapper, CrmPaymentMapper paymentMapper,
                              CrmInvoiceMapper invoiceMapper, CrmCustomerMapper customerMapper,
                              SysUserMapper userMapper) {
        this.contractMapper = contractMapper;
        this.paymentMapper = paymentMapper;
        this.invoiceMapper = invoiceMapper;
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
    }

    public PageResult<CrmContract> page(int pageNum, int pageSize, String contractName, String status) {
        LambdaQueryWrapper<CrmContract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(contractName)) {
            wrapper.like(CrmContract::getContractName, contractName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CrmContract::getStatus, status);
        }
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(CrmContract::getOwnerId, SecurityUtils.getUserId());
        }
        wrapper.orderByDesc(CrmContract::getCreateTime);
        Page<CrmContract> page = contractMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public void save(CrmContract contract) {
        if (contract.getOwnerId() == null) {
            contract.setOwnerId(SecurityUtils.getUserId());
        }
        if (!StringUtils.hasText(contract.getStatus())) {
            contract.setStatus("草稿");
        }
        contractMapper.insert(contract);
    }

    public void update(CrmContract contract) {
        contractMapper.updateById(contract);
    }

    public void approve(Long id, String status) {
        CrmContract c = new CrmContract();
        c.setId(id);
        c.setStatus(status);
        contractMapper.updateById(c);
    }

    public void delete(Long id) {
        contractMapper.deleteById(id);
    }

    public void addPayment(CrmPayment payment) {
        payment.setCreateBy(SecurityUtils.getUserId());
        paymentMapper.insert(payment);
    }

    public List<CrmPayment> listPayments(Long contractId) {
        return paymentMapper.selectList(new LambdaQueryWrapper<CrmPayment>()
                .eq(CrmPayment::getContractId, contractId)
                .orderByDesc(CrmPayment::getPaymentDate));
    }

    public void addInvoice(CrmInvoice invoice) {
        invoiceMapper.insert(invoice);
    }

    public List<CrmInvoice> listInvoices(Long contractId) {
        return invoiceMapper.selectList(new LambdaQueryWrapper<CrmInvoice>()
                .eq(CrmInvoice::getContractId, contractId)
                .orderByDesc(CrmInvoice::getInvoiceDate));
    }

    private void fillExtra(List<CrmContract> list) {
        Map<Long, String> customerMap = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        list.forEach(c -> {
            c.setCustomerName(customerMap.get(c.getCustomerId()));
            c.setOwnerName(userMap.get(c.getOwnerId()));
        });
    }

    public void export(HttpServletResponse response, String contractName) throws Exception {
        LambdaQueryWrapper<CrmContract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(contractName)) {
            wrapper.like(CrmContract::getContractName, contractName);
        }
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(CrmContract::getOwnerId, SecurityUtils.getUserId());
        }
        List<CrmContract> list = contractMapper.selectList(wrapper);
        fillExtra(list);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("合同列表");
        String[] headers = {"合同编号", "合同名称", "客户", "金额", "状态", "负责人", "签订日期"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        for (int i = 0; i < list.size(); i++) {
            CrmContract c = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(c.getContractNo());
            row.createCell(1).setCellValue(c.getContractName());
            row.createCell(2).setCellValue(c.getCustomerName());
            row.createCell(3).setCellValue(c.getAmount() != null ? c.getAmount().doubleValue() : 0);
            row.createCell(4).setCellValue(c.getStatus());
            row.createCell(5).setCellValue(c.getOwnerName());
            row.createCell(6).setCellValue(c.getSignDate() != null ? c.getSignDate().toString() : "");
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("合同列表.xlsx", "UTF-8"));
        wb.write(response.getOutputStream());
        wb.close();
    }

    public int importExcel(MultipartFile file) throws Exception {
        Workbook wb = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = wb.getSheetAt(0);
        int count = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            CrmContract c = new CrmContract();
            c.setContractNo(getCellValue(row.getCell(0)));
            c.setContractName(getCellValue(row.getCell(1)));
            c.setStatus(StringUtils.hasText(getCellValue(row.getCell(3))) ? getCellValue(row.getCell(3)) : "草稿");
            if (StringUtils.hasText(getCellValue(row.getCell(2)))) {
                Long customerId = customerMapper.selectList(new LambdaQueryWrapper<CrmCustomer>()
                        .eq(CrmCustomer::getCustomerName, getCellValue(row.getCell(2))))
                        .stream().findFirst().map(CrmCustomer::getId).orElse(null);
                c.setCustomerId(customerId);
            }
            if (StringUtils.hasText(getCellValue(row.getCell(3)))) {
                try { c.setAmount(new BigDecimal(getCellValue(row.getCell(3)))); } catch (Exception ignored) {}
            }
            c.setOwnerId(SecurityUtils.getUserId());
            if (StringUtils.hasText(c.getContractName())) {
                contractMapper.insert(c);
                count++;
            }
        }
        wb.close();
        return count;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
