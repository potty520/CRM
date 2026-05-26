package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmFollow;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmFollowMapper;
import com.crm.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmFollowService {

    private final CrmFollowMapper followMapper;
    private final CrmCustomerMapper customerMapper;
    private final SysUserMapper userMapper;

    public CrmFollowService(CrmFollowMapper followMapper, CrmCustomerMapper customerMapper,
                            SysUserMapper userMapper) {
        this.followMapper = followMapper;
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
    }

    public PageResult<CrmFollow> page(int pageNum, int pageSize, Long customerId) {
        LambdaQueryWrapper<CrmFollow> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(CrmFollow::getCustomerId, customerId);
        }
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(CrmFollow::getCreateBy, SecurityUtils.getUserId());
        }
        wrapper.orderByDesc(CrmFollow::getCreateTime);
        Page<CrmFollow> page = followMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public List<CrmFollow> listByCustomer(Long customerId) {
        List<CrmFollow> list = followMapper.selectList(new LambdaQueryWrapper<CrmFollow>()
                .eq(CrmFollow::getCustomerId, customerId)
                .orderByDesc(CrmFollow::getCreateTime));
        fillExtra(list);
        return list;
    }

    public void save(CrmFollow follow) {
        follow.setCreateBy(SecurityUtils.getUserId());
        followMapper.insert(follow);
    }

    public void delete(Long id) {
        followMapper.deleteById(id);
    }

    public List<CrmFollow> pendingReminders() {
        LambdaQueryWrapper<CrmFollow> wrapper = new LambdaQueryWrapper<CrmFollow>()
                .le(CrmFollow::getNextFollowTime, LocalDateTime.now().plusDays(3))
                .ge(CrmFollow::getNextFollowTime, LocalDateTime.now().minusDays(1));
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(CrmFollow::getCreateBy, SecurityUtils.getUserId());
        }
        List<CrmFollow> list = followMapper.selectList(wrapper.orderByAsc(CrmFollow::getNextFollowTime));
        fillExtra(list);
        return list;
    }

    private void fillExtra(List<CrmFollow> list) {
        Map<Long, String> customerMap = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        list.forEach(f -> {
            f.setCustomerName(customerMap.get(f.getCustomerId()));
            f.setCreateByName(userMap.get(f.getCreateBy()));
        });
    }
}
