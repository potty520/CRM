package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.entity.SysRole;
import com.crm.mapper.SysMenuMapper;
import com.crm.mapper.SysRoleMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final JdbcTemplate jdbcTemplate;

    public SysRoleService(SysRoleMapper roleMapper, SysMenuMapper menuMapper, JdbcTemplate jdbcTemplate) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResult<SysRole> page(int pageNum, int pageSize) {
        Page<SysRole> page = roleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreateTime));
        page.getRecords().forEach(r -> r.setMenuIds(menuMapper.selectMenuIdsByRoleId(r.getId())));
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1));
    }

    @Transactional
    public void save(SysRole role) {
        roleMapper.insert(role);
        saveRoleMenus(role.getId(), role.getMenuIds());
    }

    @Transactional
    public void update(SysRole role) {
        roleMapper.updateById(role);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", role.getId());
        saveRoleMenus(role.getId(), role.getMenuIds());
    }

    public void delete(Long id) {
        if ("admin".equals(roleMapper.selectById(id).getRoleCode())) {
            throw new BusinessException("不能删除管理员角色");
        }
        roleMapper.deleteById(id);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", id);
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null) return;
        for (Long menuId : menuIds) {
            jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", roleId, menuId);
        }
    }
}
