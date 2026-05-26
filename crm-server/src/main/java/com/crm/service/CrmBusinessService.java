package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.CrmBusiness;
import com.crm.entity.CrmContract;
import com.crm.entity.CrmCustomer;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmBusinessMapper;
import com.crm.mapper.CrmContractMapper;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmBusinessService {

    public static final List<String> STAGES = Arrays.asList(
            "初步接触", "需求确认", "方案报价", "商务谈判", "合同签订", "成交");

    private final CrmBusinessMapper businessMapper;
    private final CrmCustomerMapper customerMapper;
    private final CrmContractMapper contractMapper;
    private final SysUserMapper userMapper;

    public CrmBusinessService(CrmBusinessMapper businessMapper, CrmCustomerMapper customerMapper,
                              CrmContractMapper contractMapper, SysUserMapper userMapper) {
        this.businessMapper = businessMapper;
        this.customerMapper = customerMapper;
        this.contractMapper = contractMapper;
        this.userMapper = userMapper;
    }

    public PageResult<CrmBusiness> page(int pageNum, int pageSize, String businessName, String stage) {
        LambdaQueryWrapper<CrmBusiness> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(businessName)) {
            wrapper.like(CrmBusiness::getBusinessName, businessName);
        }
        if (StringUtils.hasText(stage)) {
            wrapper.eq(CrmBusiness::getStage, stage);
        }
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(CrmBusiness::getOwnerId, SecurityUtils.getUserId());
        }
        wrapper.orderByDesc(CrmBusiness::getCreateTime);
        Page<CrmBusiness> page = businessMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public void save(CrmBusiness business) {
        if (business.getOwnerId() == null) {
            business.setOwnerId(SecurityUtils.getUserId());
        }
        if (!StringUtils.hasText(business.getStage())) {
            business.setStage("初步接触");
        }
        businessMapper.insert(business);
    }

    public void update(CrmBusiness business) {
        businessMapper.updateById(business);
    }

    public void updateStage(Long id, String stage) {
        CrmBusiness b = new CrmBusiness();
        b.setId(id);
        b.setStage(stage);
        int idx = STAGES.indexOf(stage);
        if (idx >= 0) {
            b.setProbability(Math.min(90, (idx + 1) * 15));
        }
        businessMapper.updateById(b);
    }

    public void delete(Long id) {
        businessMapper.deleteById(id);
    }

    public CrmContract convertToContract(Long businessId) {
        CrmBusiness business = businessMapper.selectById(businessId);
        CrmContract contract = new CrmContract();
        contract.setContractNo("HT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", businessId));
        contract.setContractName(business.getBusinessName() + "-合同");
        contract.setCustomerId(business.getCustomerId());
        contract.setBusinessId(businessId);
        contract.setAmount(business.getAmount());
        contract.setStatus("草稿");
        contract.setOwnerId(business.getOwnerId());
        contractMapper.insert(contract);
        updateStage(businessId, "合同签订");
        return contract;
    }

    private void fillExtra(List<CrmBusiness> list) {
        Map<Long, String> customerMap = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        list.forEach(b -> {
            b.setCustomerName(customerMap.get(b.getCustomerId()));
            b.setOwnerName(userMap.get(b.getOwnerId()));
        });
    }
}
