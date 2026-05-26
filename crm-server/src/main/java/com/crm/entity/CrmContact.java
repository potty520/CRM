package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("crm_contact")
public class CrmContact {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private String contactName;
    private String mobile;
    private String email;
    private String position;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String customerName;
}
