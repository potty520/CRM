package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crm_payment")
public class CrmPayment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
}
