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
public class ErpServiceImpl implements ErpService {

    private final CrmCustomerMapper customerMapper;
    private final CrmBusinessMapper businessMapper;
    private final CrmContractMapper contractMapper;
    private final JdbcTemplate jdbcTemplate;

    public ErpServiceImpl(CrmCustomerMapper customerMapper, CrmBusinessMapper businessMapper,
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
    public List<Map<String, Object>> getInventoryStats() {
        String sql = "SELECT '库存商品' AS name, COUNT(*) AS value FROM crm_business WHERE stage = '成交'";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getSalesStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        String sql = "SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, " +
                "COUNT(*) AS salesCount, " +
                "SUM(amount) AS totalAmount " +
                "FROM crm_contract " +
                "WHERE status = '已生效' " +
                "AND create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
                "ORDER BY month ASC";

        List<Map<String, Object>> dbData = jdbcTemplate.queryForList(sql);
        Set<String> existingMonths = dbData.stream()
                .map(m -> (String) m.get("month"))
                .collect(Collectors.toSet());

        for (int i = 11; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i);
            String monthStr = month.format(fmt);
            final int monthIndex = 11 - i;

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);

            if (monthIndex < dbData.size()) {
                Map<String, Object> dbRow = dbData.get(monthIndex);
                item.put("salesCount", dbRow.get("salesCount"));
                item.put("totalAmount", dbRow.get("totalAmount"));
            } else {
                item.put("salesCount", 0);
                item.put("totalAmount", BigDecimal.ZERO);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> getPurchaseStats() {
        Map<String, Object> data = new HashMap<>();

        String totalSql = "SELECT COUNT(*) FROM crm_business WHERE stage = '成交'";
        Long totalPurchases = jdbcTemplate.queryForObject(totalSql, Long.class);

        data.put("totalPurchases", totalPurchases != null ? totalPurchases : 0L);
        data.put("purchaseStatus", "正常");

        return data;
    }

    @Override
    public List<Map<String, Object>> getFinancialStats() {
        List<Map<String, Object>> result = new ArrayList<>();

        BigDecimal totalContractAmount = contractMapper.selectList(
                new LambdaQueryWrapper<CrmContract>()
                        .eq(CrmContract::getStatus, "已生效"))
                .stream()
                .map(CrmContract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> incomeItem = new HashMap<>();
        incomeItem.put("type", "收入");
        incomeItem.put("amount", totalContractAmount);
        result.add(incomeItem);

        Map<String, Object> expenseItem = new HashMap<>();
        expenseItem.put("type", "支出");
        expenseItem.put("amount", BigDecimal.ZERO);
        result.add(expenseItem);

        Map<String, Object> profitItem = new HashMap<>();
        profitItem.put("type", "利润");
        profitItem.put("amount", totalContractAmount);
        result.add(profitItem);

        return result;
    }
}
