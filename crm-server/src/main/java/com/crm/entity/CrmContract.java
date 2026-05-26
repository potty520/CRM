package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crm_contract")
public class CrmContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    private String contractName;
    private Long customerId;
    private Long businessId;
    private BigDecimal amount;
    private String status;
    private Long ownerId;
    private LocalDate signDate;
    private String attachment;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String ownerName;
}
