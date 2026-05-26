package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_inventory")
public class ErpInventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal stockQty;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private String unit;
    private String batchNo;
    private LocalDateTime productionDate;
    private LocalDateTime expireDate;
    private LocalDateTime lastInbound;
    private Integer status;
    private Long deptId;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}