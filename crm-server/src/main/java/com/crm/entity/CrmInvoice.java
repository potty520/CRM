package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crm_invoice")
public class CrmInvoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private String invoiceNo;
    private BigDecimal amount;
    private LocalDate invoiceDate;
    private String remark;
    private LocalDateTime createTime;
}
