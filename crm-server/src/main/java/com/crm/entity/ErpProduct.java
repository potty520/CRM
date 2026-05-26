package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_product")
public class ErpProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String productCode;
    private String productName;
    private String spec;
    private String unit;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private String description;
    private Long creatorId;
    private Long deptId;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
