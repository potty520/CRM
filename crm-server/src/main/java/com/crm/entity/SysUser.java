package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Long deptId;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private java.util.List<Long> roleIds;
}
