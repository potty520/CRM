package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmFollow;
import com.crm.service.CrmFollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class CrmFollowController {

    private final CrmFollowService followService;

    public CrmFollowController(CrmFollowService followService) {
        this.followService = followService;
    }

    @GetMapping("/page")
    public Result<PageResult<CrmFollow>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long customerId) {
        return Result.ok(followService.page(pageNum, pageSize, customerId));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<CrmFollow>> listByCustomer(@PathVariable Long customerId) {
        return Result.ok(followService.listByCustomer(customerId));
    }

    @GetMapping("/pending")
    public Result<List<CrmFollow>> pending() {
        return Result.ok(followService.pendingReminders());
    }

    @PostMapping
    public Result<Void> save(@RequestBody CrmFollow follow) {
        followService.save(follow);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        followService.delete(id);
        return Result.ok();
    }
}
