package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.CrmCustomer;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.SysUserMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmCustomerService {

    private final CrmCustomerMapper customerMapper;
    private final SysUserMapper userMapper;

    public CrmCustomerService(CrmCustomerMapper customerMapper, SysUserMapper userMapper) {
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
    }

    public PageResult<CrmCustomer> page(int pageNum, int pageSize, String customerName,
                                        Integer poolStatus, Long ownerId) {
        LambdaQueryWrapper<CrmCustomer> wrapper = buildWrapper(customerName, poolStatus, ownerId);
        Page<CrmCustomer> page = customerMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillOwnerName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public CrmCustomer getById(Long id) {
        CrmCustomer c = customerMapper.selectById(id);
        if (c != null) fillOwnerName(java.util.Collections.singletonList(c));
        return c;
    }

    public void save(CrmCustomer customer) {
        if (customer.getOwnerId() == null) {
            customer.setOwnerId(SecurityUtils.getUserId());
        }
        if (customer.getDeptId() == null) {
            customer.setDeptId(SecurityUtils.getLoginUser().getDeptId());
        }
        customer.setPoolStatus(0);
        customerMapper.insert(customer);
    }

    public void update(CrmCustomer customer) {
        customerMapper.updateById(customer);
    }

    public void delete(Long id) {
        customerMapper.deleteById(id);
    }

    public void assign(Long customerId, Long ownerId) {
        CrmCustomer c = new CrmCustomer();
        c.setId(customerId);
        c.setOwnerId(ownerId);
        c.setPoolStatus(0);
        customerMapper.updateById(c);
    }

    public void toPool(Long id) {
        CrmCustomer c = new CrmCustomer();
        c.setId(id);
        c.setPoolStatus(1);
        c.setOwnerId(null);
        customerMapper.updateById(c);
    }

    public void claimFromPool(Long id) {
        CrmCustomer c = new CrmCustomer();
        c.setId(id);
        c.setPoolStatus(0);
        c.setOwnerId(SecurityUtils.getUserId());
        customerMapper.updateById(c);
    }

    public void export(HttpServletResponse response, String customerName) throws Exception {
        List<CrmCustomer> list = customerMapper.selectList(buildWrapper(customerName, null, null));
        fillOwnerName(list);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("客户列表");
        String[] headers = {"客户名称", "联系人", "手机号", "邮箱", "等级", "来源", "所属销售"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        for (int i = 0; i < list.size(); i++) {
            CrmCustomer c = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(c.getCustomerName());
            row.createCell(1).setCellValue(c.getContactName());
            row.createCell(2).setCellValue(c.getMobile());
            row.createCell(3).setCellValue(c.getEmail());
            row.createCell(4).setCellValue(c.getLevel() != null ? c.getLevel().toString() : "");
            row.createCell(5).setCellValue(c.getSource());
            row.createCell(6).setCellValue(c.getOwnerName());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("客户列表.xlsx", "UTF-8"));
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
            CrmCustomer c = new CrmCustomer();
            c.setCustomerName(getCellValue(row.getCell(0)));
            c.setContactName(getCellValue(row.getCell(1)));
            c.setMobile(getCellValue(row.getCell(2)));
            c.setEmail(getCellValue(row.getCell(3)));
            c.setSource(getCellValue(row.getCell(4)));
            c.setOwnerId(SecurityUtils.getUserId());
            c.setPoolStatus(0);
            if (StringUtils.hasText(c.getCustomerName())) {
                customerMapper.insert(c);
                count++;
            }
        }
        wb.close();
        return count;
    }

    public long countToday() {
        return customerMapper.selectCount(new LambdaQueryWrapper<CrmCustomer>()
                .ge(CrmCustomer::getCreateTime, LocalDate.now().atStartOfDay()));
    }

    private LambdaQueryWrapper<CrmCustomer> buildWrapper(String customerName, Integer poolStatus, Long ownerId) {
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(customerName)) {
            wrapper.like(CrmCustomer::getCustomerName, customerName);
        }
        if (poolStatus != null) {
            wrapper.eq(CrmCustomer::getPoolStatus, poolStatus);
        }
        if (ownerId != null) {
            wrapper.eq(CrmCustomer::getOwnerId, ownerId);
        }
        if (!SecurityUtils.isAdmin()) {
            // 数据权限：非管理员按部门过滤，公海客户（pool_status=1）允许跨部门可见
            wrapper.and(w -> w.eq(CrmCustomer::getOwnerId, SecurityUtils.getUserId())
                    .eq(CrmCustomer::getDeptId, SecurityUtils.getDeptId())
                    .or().eq(CrmCustomer::getPoolStatus, 1));
        }
        wrapper.orderByDesc(CrmCustomer::getCreateTime);
        return wrapper;
    }

    private void fillOwnerName(List<CrmCustomer> list) {
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        list.forEach(c -> c.setOwnerName(userMap.get(c.getOwnerId())));
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
