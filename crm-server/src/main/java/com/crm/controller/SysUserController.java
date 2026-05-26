package com.crm.controller;

import com.crm.aspect.OperLogAspect.OperLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.SysUser;
import com.crm.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username) {
        return Result.ok(userService.page(pageNum, pageSize, username));
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.ok(userService.listAll());
    }

    @PostMapping
    @OperLog(module = "用户管理", operation = "新增用户")
    public Result<Void> save(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @PutMapping
    @OperLog(module = "用户管理", operation = "编辑用户")
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.ok();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "用户管理", operation = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }
}
