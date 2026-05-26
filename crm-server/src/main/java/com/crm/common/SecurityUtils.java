package com.crm.common;

import com.crm.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        throw new BusinessException("未登录");
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static LoginUser getCurrentUser() {
        return getLoginUser();
    }

    public static boolean isAdmin() {
        return getLoginUser().getRoles().contains("admin");
    }

    public static List<String> getRoles() {
        return getLoginUser().getRoles();
    }

    public static Long getDeptId() {
        return getLoginUser().getDeptId();
    }
}
