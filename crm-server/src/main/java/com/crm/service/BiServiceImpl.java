package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.entity.CrmBusiness;
import com.crm.entity.CrmContract;
import com.crm.entity.CrmCustomer;
import com.crm.mapper.CrmBusinessMapper;
import com.crm.mapper.CrmContractMapper;
import com.crm.mapper.CrmCustomerMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BiServiceImpl implements BiService {

    private final CrmCustomerMapper customerMapper;
    private final CrmBusinessMapper businessMapper;
    private final CrmContractMapper contractMapper;
    private final JdbcTemplate jdbcTemplate;

    public BiServiceImpl(CrmCustomerMapper customerMapper, CrmBusinessMapper businessMapper,
                        CrmContractMapper contractMapper, JdbcTemplate jdbcTemplate) {
        this.customerMapper = customerMapper;
        this.businessMapper = businessMapper;
        this.contractMapper = contractMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> data = new HashMap<>();

        long totalCustomers = customerMapper.selectCount(null);
        long totalBusiness = businessMapper.selectCount(null);
        long totalContracts = contractMapper.selectCount(null);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long todayNewCustomers = customerMapper.selectCount(new LambdaQueryWrapper<CrmCustomer>()
                .ge(CrmCustomer::getCreateTime, todayStart));

        long monthNewCustomers = customerMapper.selectCount(new LambdaQueryWrapper<CrmCustomer>()
                .ge(CrmCustomer::getCreateTime, monthStart));

        BigDecimal monthContractAmount = contractMapper.selectList(
                new LambdaQueryWrapper<CrmContract>()
                        .ge(CrmContract::getCreateTime, monthStart)
                        .eq(CrmContract::getStatus, "已生效"))
                .stream()
                .map(CrmContract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalContractAmount = contractMapper.selectList(
                new LambdaQueryWrapper<CrmContract>()
                        .eq(CrmContract::getStatus, "已生效"))
                .stream()
                .map(CrmContract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("totalCustomers", totalCustomers);
        data.put("totalBusiness", totalBusiness);
        data.put("totalContracts", totalContracts);
        data.put("todayNewCustomers", todayNewCustomers);
        data.put("monthNewCustomers", monthNewCustomers);
        data.put("monthContractAmount", monthContractAmount);
        data.put("totalContractAmount", totalContractAmount);

        return data;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        String sql = "SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, " +
                "COUNT(*) AS customerCount, " +
                "SUM(CASE WHEN status IN ('已生效', '已完成') THEN 1 ELSE 0 END) AS contractCount " +
                "FROM crm_contract " +
                "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
                "ORDER BY month ASC";

        List<Map<String, Object>> dbData = jdbcTemplate.queryForList(sql, months);
        Set<String> existingMonths = dbData.stream()
                .map(m -> (String) m.get("month"))
                .collect(Collectors.toSet());

        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i);
            String monthStr = month.format(fmt);
            final int monthIndex = months - 1 - i;

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);

            if (monthIndex < dbData.size()) {
                Map<String, Object> dbRow = dbData.get(monthIndex);
                item.put("customerCount", dbRow.get("customerCount"));
                item.put("contractCount", dbRow.get("contractCount"));
            } else {
                item.put("customerCount", 0);
                item.put("contractCount", 0);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTeamRanking() {
        String sql = "SELECT u.real_name AS name, " +
                "COUNT(DISTINCT c.id) AS contractCount, " +
                "IFNULL(SUM(c.amount), 0) AS totalAmount, " +
                "COUNT(DISTINCT b.id) AS businessCount " +
                "FROM sys_user u " +
                "LEFT JOIN crm_contract c ON u.id = c.owner_id AND c.status = '已生效' " +
                "LEFT JOIN crm_business b ON u.id = b.owner_id " +
                "WHERE u.status = 1 " +
                "GROUP BY u.id, u.real_name " +
                "ORDER BY totalAmount DESC " +
                "LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getFunnelData() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> stages = Arrays.asList("初步接触", "需求确认", "方案报价", "商务谈判", "合同签订", "成交");

        for (String stage : stages) {
            Map<String, Object> item = new HashMap<>();
            item.put("stage", stage);
            item.put("count", businessMapper.selectCount(new LambdaQueryWrapper<CrmBusiness>()
                    .eq(CrmBusiness::getStage, stage)));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCustomerSourceStats() {
        String sql = "SELECT source AS name, COUNT(*) AS value " +
                "FROM crm_customer " +
                "WHERE source IS NOT NULL AND source != '' " +
                "GROUP BY source " +
                "ORDER BY value DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getCustomerLevelStats() {
        String sql = "SELECT level AS name, COUNT(*) AS value " +
                "FROM crm_customer " +
                "WHERE level IS NOT NULL AND level != '' " +
                "GROUP BY level " +
                "ORDER BY value DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> getBusinessWinRate() {
        Map<String, Object> data = new HashMap<>();

        String totalSql = "SELECT COUNT(*) AS total FROM crm_business";
        String winSql = "SELECT COUNT(*) AS wins FROM crm_business WHERE stage = '成交'";

        Long total = jdbcTemplate.queryForObject(totalSql, Long.class);
        Long wins = jdbcTemplate.queryForObject(winSql, Long.class);

        long totalCount = total != null ? total : 0L;
        long winCount = wins != null ? wins : 0L;

        double winRate = totalCount > 0 ? (double) winCount / totalCount * 100 : 0.0;

        data.put("totalBusiness", totalCount);
        data.put("winCount", winCount);
        data.put("winRate", Math.round(winRate * 100.0) / 100.0);

        return data;
    }
}
