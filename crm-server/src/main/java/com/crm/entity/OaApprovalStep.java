package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("oa_approval_step")
public class OaApprovalStep {
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    private Long flowId;
    private Integer stepNo;
    private Long approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String status;
    private String opinion;

    @TableLogic
    private Integer deleted;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}