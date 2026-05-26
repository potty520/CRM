package com.crm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.entity.SysMenu;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.mapper.SysMenuMapper;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public UserDetailsServiceImpl(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        loginUser.setPermissions(menus.stream()
                .map(SysMenu::getPerms)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        return loginUser;
    }
}
