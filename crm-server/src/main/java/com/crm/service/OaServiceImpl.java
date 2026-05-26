package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.*;
import com.crm.mapper.*;
import com.crm.security.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OaServiceImpl implements OaService {

    private final OaApprovalFlowMapper flowMapper;
    private final OaApprovalStepMapper stepMapper;
    private final OaApprovalRecordMapper recordMapper;
    private final SysUserMapper userMapper;

    public OaServiceImpl(OaApprovalFlowMapper flowMapper, OaApprovalStepMapper stepMapper,
                     OaApprovalRecordMapper recordMapper, SysUserMapper userMapper) {
        this.flowMapper = flowMapper;
        this.stepMapper = stepMapper;
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public void submitApproval(OaApprovalFlow flow, List<OaApprovalStep> steps) {
        LoginUser currentUser = SecurityUtils.getCurrentUser();
        flow.setApplicantId(currentUser.getUserId());
        flow.setApplicantName(currentUser.getRealName());
        flow.setApplyTime(LocalDateTime.now());
        flow.setStatus("pending");
        flow.setCurrentStep(1);
        flowMapper.insert(flow);

        for (int i = 0; i < steps.size(); i++) {
            OaApprovalStep step = steps.get(i);
            step.setFlowId(flow.getId());
            step.setStepNo(i + 1);
            step.setStatus("pending");
            stepMapper.insert(step);
        }

        OaApprovalRecord record = new OaApprovalRecord();
        record.setFlowId(flow.getId());
        record.setOperatorId(currentUser.getUserId());
        record.setOperatorName(currentUser.getRealName());
        record.setAction("submit");
        record.setContent("提交审批: " + flow.getFlowName());
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    @Transactional
    public void approve(Long flowId, Long stepId, String opinion) {
        LoginUser currentUser = SecurityUtils.getCurrentUser();
        OaApprovalStep step = stepMapper.selectById(stepId);
        step.setStatus("approved");
        step.setApproveTime(LocalDateTime.now());
        step.setOpinion(opinion);
        stepMapper.updateById(step);

        OaApprovalFlow flow = flowMapper.selectById(flowId);
        List<OaApprovalStep> allSteps = stepMapper.selectList(
            new LambdaQueryWrapper<OaApprovalStep>()
                .eq(OaApprovalStep::getFlowId, flowId)
                .orderByAsc(OaApprovalStep::getStepNo)
        );

        boolean allApproved = allSteps.stream().allMatch(s -> "approved".equals(s.getStatus()));
        if (allApproved) {
            flow.setStatus("approved");
        } else {
            flow.setCurrentStep(flow.getCurrentStep() + 1);
        }
        flowMapper.updateById(flow);

        OaApprovalRecord record = new OaApprovalRecord();
        record.setFlowId(flowId);
        record.setStepId(stepId);
        record.setOperatorId(currentUser.getUserId());
        record.setOperatorName(currentUser.getRealName());
        record.setAction("approve");
        record.setContent(opinion != null ? opinion : "同意");
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    @Transactional
    public void reject(Long flowId, Long stepId, String opinion) {
        LoginUser currentUser = SecurityUtils.getCurrentUser();
        OaApprovalStep step = stepMapper.selectById(stepId);
        step.setStatus("rejected");
        step.setApproveTime(LocalDateTime.now());
        step.setOpinion(opinion);
        stepMapper.updateById(step);

        OaApprovalFlow flow = flowMapper.selectById(flowId);
        flow.setStatus("rejected");
        flowMapper.updateById(flow);

        OaApprovalRecord record = new OaApprovalRecord();
        record.setFlowId(flowId);
        record.setStepId(stepId);
        record.setOperatorId(currentUser.getUserId());
        record.setOperatorName(currentUser.getRealName());
        record.setAction("reject");
        record.setContent(opinion != null ? opinion : "驳回");
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    public PageResult<Map<String, Object>> getFlowPage(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<OaApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(OaApprovalFlow::getStatus, status);
        }
        wrapper.orderByDesc(OaApprovalFlow::getApplyTime);
        Page<OaApprovalFlow> page = flowMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> records = page.getRecords().stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("flowName", f.getFlowName());
            m.put("flowType", f.getFlowType());
            m.put("bizId", f.getBizId());
            m.put("bizType", f.getBizType());
            m.put("applicantId", f.getApplicantId());
            m.put("applicantName", f.getApplicantName());
            m.put("applyTime", f.getApplyTime());
            m.put("status", f.getStatus());
            m.put("currentStep", f.getCurrentStep());
            m.put("remark", f.getRemark());
            return m;
        }).collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    public Map<String, Object> getFlowDetail(Long flowId) {
        OaApprovalFlow flow = flowMapper.selectById(flowId);
        List<OaApprovalStep> steps = stepMapper.selectList(
            new LambdaQueryWrapper<OaApprovalStep>()
                .eq(OaApprovalStep::getFlowId, flowId)
                .orderByAsc(OaApprovalStep::getStepNo)
        );
        List<OaApprovalRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<OaApprovalRecord>()
                .eq(OaApprovalRecord::getFlowId, flowId)
                .orderByAsc(OaApprovalRecord::getCreateTime)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("flow", flow);
        result.put("steps", steps);
        result.put("records", records);
        return result;
    }

    public List<Map<String, Object>> getPendingApprovals(Long userId) {
        List<OaApprovalStep> pendingSteps = stepMapper.selectList(
            new LambdaQueryWrapper<OaApprovalStep>()
                .eq(OaApprovalStep::getApproverId, userId)
                .eq(OaApprovalStep::getStatus, "pending")
        );
        List<Long> flowIds = pendingSteps.stream().map(OaApprovalStep::getFlowId).distinct().collect(Collectors.toList());
        if (flowIds.isEmpty()) return List.of();
        List<OaApprovalFlow> flows = flowMapper.selectList(
            new LambdaQueryWrapper<OaApprovalFlow>()
                .in(OaApprovalFlow::getId, flowIds)
                .eq(OaApprovalFlow::getStatus, "pending")
        );
        return flows.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("flowName", f.getFlowName());
            m.put("flowType", f.getFlowType());
            m.put("applicantName", f.getApplicantName());
            m.put("applyTime", f.getApplyTime());
            m.put("status", f.getStatus());
            OaApprovalStep pendingStep = pendingSteps.stream()
                .filter(s -> s.getFlowId().equals(f.getId()))
                .findFirst().orElse(null);
            if (pendingStep != null) {
                m.put("stepId", pendingStep.getId());
                m.put("stepNo", pendingStep.getStepNo());
            }
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyApplications(Long userId) {
        List<OaApprovalFlow> flows = flowMapper.selectList(
            new LambdaQueryWrapper<OaApprovalFlow>()
                .eq(OaApprovalFlow::getApplicantId, userId)
                .orderByDesc(OaApprovalFlow::getApplyTime)
        );
        return flows.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("flowName", f.getFlowName());
            m.put("flowType", f.getFlowType());
            m.put("applyTime", f.getApplyTime());
            m.put("status", f.getStatus());
            return m;
        }).collect(Collectors.toList());
    }
}