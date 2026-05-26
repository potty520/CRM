package com.crm.controller;

import com.crm.common.Result;
import com.crm.service.BiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi")
public class BiController {

    private final BiService biService;

    public BiController(BiService biService) {
        this.biService = biService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(biService.getOverviewStats());
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "12") Integer months) {
        return Result.ok(biService.getMonthlyTrend(months));
    }

    @GetMapping("/team-ranking")
    public Result<List<Map<String, Object>>> teamRanking() {
        return Result.ok(biService.getTeamRanking());
    }

    @GetMapping("/funnel")
    public Result<List<Map<String, Object>>> funnel() {
        return Result.ok(biService.getFunnelData());
    }

    @GetMapping("/customer-source")
    public Result<List<Map<String, Object>>> customerSource() {
        return Result.ok(biService.getCustomerSourceStats());
    }

    @GetMapping("/customer-level")
    public Result<List<Map<String, Object>>> customerLevel() {
        return Result.ok(biService.getCustomerLevelStats());
    }

    @GetMapping("/business-winrate")
    public Result<Map<String, Object>> businessWinrate() {
        return Result.ok(biService.getBusinessWinRate());
    }
}