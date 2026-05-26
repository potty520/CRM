package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("oa_approval_flow")
public class OaApprovalFlow {
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    private String flowName;
    private String flowType;
    private Long bizId;
    private String bizType;
    private Long applicantId;
    private LocalDateTime applyTime;
    private String status;
    private Integer currentStep;
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String applicantName;
}