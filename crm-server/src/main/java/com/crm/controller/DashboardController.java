package com.crm.controller;

import com.crm.common.Result;
import com.crm.entity.CrmFollow;
import com.crm.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(dashboardService.overview());
    }

    @GetMapping("/sales-rank")
    public Result<List<Map<String, Object>>> salesRank() {
        return Result.ok(dashboardService.salesRank());
    }

    @GetMapping("/funnel")
    public Result<List<Map<String, Object>>> funnel() {
        return Result.ok(dashboardService.funnel());
    }

    @GetMapping("/customer-trend")
    public Result<List<Map<String, Object>>> customerTrend() {
        return Result.ok(dashboardService.customerTrend());
    }

    @GetMapping("/payment-stats")
    public Result<List<Map<String, Object>>> paymentStats() {
        return Result.ok(dashboardService.paymentStats());
    }

    @GetMapping("/pending-follows")
    public Result<List<CrmFollow>> pendingFollows() {
        return Result.ok(dashboardService.pendingFollows());
    }
}
