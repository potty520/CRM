package com.crm.service;

import java.util.List;
import java.util.Map;

public interface BiService {

    Map<String, Object> getOverviewStats();

    List<Map<String, Object>> getMonthlyTrend(int months);

    List<Map<String, Object>> getTeamRanking();

    List<Map<String, Object>> getFunnelData();

    List<Map<String, Object>> getCustomerSourceStats();

    List<Map<String, Object>> getCustomerLevelStats();

    Map<String, Object> getBusinessWinRate();
}
