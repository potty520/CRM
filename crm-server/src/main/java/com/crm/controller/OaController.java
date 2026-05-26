package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.common.SecurityUtils;
import com.crm.entity.OaApprovalFlow;
import com.crm.entity.OaApprovalStep;
import com.crm.service.OaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oa")
public class OaController {

    private final OaService oaService;

    public OaController(OaService oaService) {
        this.oaService = oaService;
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> params) {
        OaApprovalFlow flow = new OaApprovalFlow();
        flow.setFlowName((String) params.get("flowName"));
        flow.setFlowType((String) params.get("flowType"));
        Object bizId = params.get("bizId");
        if (bizId != null) flow.setBizId(Long.valueOf(bizId.toString()));
        flow.setBizType((String) params.get("bizType"));
        flow.setRemark((String) params.get("remark"));

        List<Map> stepsData = (List<Map>) params.get("steps");
        List<OaApprovalStep> steps = stepsData.stream().map(s -> {
            OaApprovalStep step = new OaApprovalStep();
            step.setApproverId(Long.valueOf(s.get("approverId").toString()));
            step.setApproverName((String) s.get("approverName"));
            return step;
        }).toList();
        oaService.submitApproval(flow, steps);
        return Result.ok();
    }

    @PutMapping("/approve/{flowId}/{stepId}")
    public Result<?> approve(@PathVariable Long flowId, @PathVariable Long stepId,
                             @RequestParam(required = false) String opinion) {
        oaService.approve(flowId, stepId, opinion);
        return Result.ok();
    }

    @PutMapping("/reject/{flowId}/{stepId}")
    public Result<?> reject(@PathVariable Long flowId, @PathVariable Long stepId,
                            @RequestParam(required = false) String opinion) {
        oaService.reject(flowId, stepId, opinion);
        return Result.ok();
    }

    @GetMapping("/flow/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.ok(oaService.getFlowPage(pageNum, pageSize, status));
    }

    @GetMapping("/flow/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(oaService.getFlowDetail(id));
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending() {
        return Result.ok(oaService.getPendingApprovals(SecurityUtils.getUserId()));
    }

    @GetMapping("/my-applications")
    public Result<List<Map<String, Object>>> myApplications() {
        return Result.ok(oaService.getMyApplications(SecurityUtils.getUserId()));
    }
}