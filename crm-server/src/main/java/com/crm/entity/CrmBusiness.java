package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crm_business")
public class CrmBusiness {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String businessName;
    private Long customerId;
    private BigDecimal amount;
    private String stage;
    private Integer probability;
    private Long ownerId;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String ownerName;
}
