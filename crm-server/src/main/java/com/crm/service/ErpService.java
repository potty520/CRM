package com.crm.service;

import java.util.List;
import java.util.Map;

public interface ErpService {

    Map<String, Object> getOverviewStats();

    List<Map<String, Object>> getInventoryStats();

    List<Map<String, Object>> getSalesStats();

    Map<String, Object> getPurchaseStats();

    List<Map<String, Object>> getFinancialStats();
}
