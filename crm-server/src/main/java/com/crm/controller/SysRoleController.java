package com.crm.controller;

import com.crm.aspect.OperLogAspect.OperLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.SysRole;
import com.crm.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(roleService.page(pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @PostMapping
    @OperLog(module = "角色管理", operation = "新增角色")
    public Result<Void> save(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @PutMapping
    @OperLog(module = "角色管理", operation = "编辑角色")
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.update(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "角色管理", operation = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }
}
