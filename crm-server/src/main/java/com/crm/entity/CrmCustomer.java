package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("crm_customer")
public class CrmCustomer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerName;
    private String contactName;
    private String mobile;
    private String email;
    private String address;
    private Integer level;
    private String source;
    private Long ownerId;
    private Long deptId;
    private Integer poolStatus;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String ownerName;
}
