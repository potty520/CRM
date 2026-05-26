package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.entity.*;
import com.crm.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final CrmCustomerMapper customerMapper;
    private final CrmBusinessMapper businessMapper;
    private final CrmContractMapper contractMapper;
    private final CrmPaymentMapper paymentMapper;
    private final CrmFollowMapper followMapper;
    private final JdbcTemplate jdbcTemplate;

    public DashboardService(CrmCustomerMapper customerMapper, CrmBusinessMapper businessMapper,
                            CrmContractMapper contractMapper, CrmPaymentMapper paymentMapper,
                            CrmFollowMapper followMapper, JdbcTemplate jdbcTemplate) {
        this.customerMapper = customerMapper;
        this.businessMapper = businessMapper;
        this.contractMapper = contractMapper;
        this.paymentMapper = paymentMapper;
        this.followMapper = followMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayCustomers", customerMapper.selectCount(new LambdaQueryWrapper<CrmCustomer>()
                .ge(CrmCustomer::getCreateTime, LocalDate.now().atStartOfDay())));
        data.put("totalCustomers", customerMapper.selectCount(null));
        data.put("totalBusiness", businessMapper.selectCount(null));

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<CrmContract> monthContracts = contractMapper.selectList(new LambdaQueryWrapper<CrmContract>()
                .ge(CrmContract::getCreateTime, monthStart)
                .eq(CrmContract::getStatus, "已生效"));
        BigDecimal monthAmount = monthContracts.stream()
                .map(CrmContract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("monthAmount", monthAmount);
        return data;
    }

    public List<Map<String, Object>> salesRank() {
        String sql = "SELECT u.real_name AS name, IFNULL(SUM(c.amount), 0) AS amount " +
                "FROM sys_user u LEFT JOIN crm_contract c ON u.id = c.owner_id AND c.status = '已生效' " +
                "WHERE u.status = 1 GROUP BY u.id, u.real_name ORDER BY amount DESC LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> funnel() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String stage : CrmBusinessService.STAGES) {
            Map<String, Object> item = new HashMap<>();
            item.put("stage", stage);
            item.put("count", businessMapper.selectCount(new LambdaQueryWrapper<CrmBusiness>()
                    .eq(CrmBusiness::getStage, stage)));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> customerTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i);
            LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            long count = customerMapper.selectCount(new LambdaQueryWrapper<CrmCustomer>()
                    .ge(CrmCustomer::getCreateTime, start)
                    .lt(CrmCustomer::getCreateTime, end));
            Map<String, Object> item = new HashMap<>();
            item.put("month", month.format(fmt));
            item.put("count", count);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> paymentStats() {
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS month, SUM(amount) AS amount " +
                "FROM crm_payment WHERE payment_date IS NOT NULL " +
                "GROUP BY DATE_FORMAT(payment_date, '%Y-%m') ORDER BY month DESC LIMIT 6";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        Collections.reverse(list);
        return list;
    }

    public List<CrmFollow> pendingFollows() {
        List<CrmFollow> list = followMapper.selectList(new LambdaQueryWrapper<CrmFollow>()
                .le(CrmFollow::getNextFollowTime, LocalDateTime.now().plusDays(7))
                .ge(CrmFollow::getNextFollowTime, LocalDateTime.now())
                .orderByAsc(CrmFollow::getNextFollowTime)
                .last("LIMIT 10"));
        return list;
    }
}
