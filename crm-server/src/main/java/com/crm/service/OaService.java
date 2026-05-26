package com.crm.service;

import com.crm.entity.OaApprovalFlow;
import com.crm.entity.OaApprovalStep;
import com.crm.common.PageResult;
import java.util.List;
import java.util.Map;

public interface OaService {
    void submitApproval(OaApprovalFlow flow, List<OaApprovalStep> steps);
    void approve(Long flowId, Long stepId, String opinion);
    void reject(Long flowId, Long stepId, String opinion);
    PageResult<Map<String, Object>> getFlowPage(int pageNum, int pageSize, String status);
    Map<String, Object> getFlowDetail(Long flowId);
    List<Map<String, Object>> getPendingApprovals(Long userId);
    List<Map<String, Object>> getMyApplications(Long userId);
}