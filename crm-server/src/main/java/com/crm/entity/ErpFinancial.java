package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("erp_financial")
public class ErpFinancial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String billNo;
    private Integer type;
    private Long relatedId;
    private String relatedBillNo;
    private Long accountId;
    private String accountName;
    private BigDecimal amount;
    private Integer paymentMethod;
    private Long customerId;
    private String customerName;
    private String remark;
    private LocalDate recordDate;
    private Integer status;
    private Long deptId;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}