package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.SecurityUtils;
import com.crm.entity.SysDept;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.mapper.SysDeptMapper;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public SysUserService(SysUserMapper userMapper, SysRoleMapper roleMapper, SysDeptMapper deptMapper,
                          PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.deptMapper = deptMapper;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResult<SysUser> page(int pageNum, int pageSize, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username)
                    .or().like(SysUser::getRealName, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillUserExtra(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public List<SysUser> listAll() {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1).orderByAsc(SysUser::getRealName));
        fillUserExtra(users);
        return users;
    }

    @Transactional
    public void save(SysUser user) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456"));
        user.setStatus(1);
        userMapper.insert(user);
        saveUserRoles(user.getId(), user.getRoleIds());
    }

    @Transactional
    public void update(SysUser user) {
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) throw new BusinessException("用户不存在");
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", user.getId());
        saveUserRoles(user.getId(), user.getRoleIds());
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        if (id.equals(SecurityUtils.getUserId())) {
            throw new BusinessException("不能删除当前登录用户");
        }
        userMapper.deleteById(id);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", id);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) return;
        for (Long roleId : roleIds) {
            jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
        }
    }

    private void fillUserExtra(List<SysUser> users) {
        Map<Long, String> deptMap = deptMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        for (SysUser u : users) {
            u.setDeptName(deptMap.get(u.getDeptId()));
            List<SysRole> roles = roleMapper.selectRolesByUserId(u.getId());
            u.setRoleIds(roles.stream().map(SysRole::getId).collect(Collectors.toList()));
        }
    }
}
