package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.SysOperLog;
import com.crm.mapper.SysOperLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/log")
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    public SysOperLogController(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    @GetMapping("/page")
    public Result<PageResult<SysOperLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperLog>().orderByDesc(SysOperLog::getCreateTime));
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }
}
