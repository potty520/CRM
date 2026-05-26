package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("oa_approval_record")
public class OaApprovalRecord {
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    private Long flowId;
    private Long stepId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String content;
    private LocalDateTime createTime;
}