package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.entity.CrmContact;
import com.crm.entity.CrmCustomer;
import com.crm.mapper.CrmContactMapper;
import com.crm.mapper.CrmCustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmContactService {

    private final CrmContactMapper contactMapper;
    private final CrmCustomerMapper customerMapper;

    public CrmContactService(CrmContactMapper contactMapper, CrmCustomerMapper customerMapper) {
        this.contactMapper = contactMapper;
        this.customerMapper = customerMapper;
    }

    public PageResult<CrmContact> page(int pageNum, int pageSize, String contactName, Long customerId) {
        LambdaQueryWrapper<CrmContact> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(contactName)) {
            wrapper.like(CrmContact::getContactName, contactName);
        }
        if (customerId != null) {
            wrapper.eq(CrmContact::getCustomerId, customerId);
        }
        wrapper.orderByDesc(CrmContact::getCreateTime);
        Page<CrmContact> page = contactMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillCustomerName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public List<CrmContact> listByCustomer(Long customerId) {
        List<CrmContact> list = contactMapper.selectList(new LambdaQueryWrapper<CrmContact>()
                .eq(CrmContact::getCustomerId, customerId));
        fillCustomerName(list);
        return list;
    }

    public void save(CrmContact contact) {
        contactMapper.insert(contact);
    }

    public void update(CrmContact contact) {
        contactMapper.updateById(contact);
    }

    public void delete(Long id) {
        contactMapper.deleteById(id);
    }

    private void fillCustomerName(List<CrmContact> list) {
        Map<Long, String> map = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));
        list.forEach(c -> c.setCustomerName(map.get(c.getCustomerId())));
    }
}
