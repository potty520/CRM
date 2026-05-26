package com.crm.service;

import com.crm.common.BusinessException;
import com.crm.dto.LoginRequest;
import com.crm.entity.SysMenu;
import com.crm.mapper.SysMenuMapper;
import com.crm.security.JwtUtils;
import com.crm.security.LoginUser;
import com.crm.security.UserDetailsServiceImpl;
import com.crm.vo.LoginVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final SysMenuMapper menuMapper;

    public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       UserDetailsServiceImpl userDetailsService, SysMenuMapper menuMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.menuMapper = menuMapper;
    }

    public LoginVO login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setRealName(loginUser.getRealName());
        vo.setRoles(loginUser.getRoles());
        vo.setMenus(buildMenuTree(menuMapper.selectMenusByUserId(loginUser.getUserId())));
        return vo;
    }

    public LoginVO getUserInfo() {
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(
                com.crm.common.SecurityUtils.getLoginUser().getUsername());
        LoginVO vo = new LoginVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setRealName(loginUser.getRealName());
        vo.setRoles(loginUser.getRoles());
        vo.setMenus(buildMenuTree(menuMapper.selectMenusByUserId(loginUser.getUserId())));
        return vo;
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() > 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        for (SysMenu root : roots) {
            root.setChildren(childrenMap.getOrDefault(root.getId(), new ArrayList<>()));
        }
        return roots;
    }
}
