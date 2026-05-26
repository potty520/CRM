package com.crm.vo;

import com.crm.entity.SysMenu;
import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<SysMenu> menus;
}
