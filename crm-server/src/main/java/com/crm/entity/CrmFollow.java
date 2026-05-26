package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("crm_follow")
public class CrmFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long businessId;
    private String content;
    private String followType;
    private LocalDateTime nextFollowTime;
    private String attachment;
    private Long createBy;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String createByName;
}
