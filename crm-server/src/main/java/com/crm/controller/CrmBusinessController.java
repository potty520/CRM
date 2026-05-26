package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmBusiness;
import com.crm.entity.CrmContract;
import com.crm.service.CrmBusinessService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class CrmBusinessController {

    private final CrmBusinessService businessService;

    public CrmBusinessController(CrmBusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/page")
    public Result<PageResult<CrmBusiness>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String stage) {
        return Result.ok(businessService.page(pageNum, pageSize, businessName, stage));
    }

    @GetMapping("/stages")
    public Result<List<String>> stages() {
        return Result.ok(CrmBusinessService.STAGES);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CrmBusiness business) {
        businessService.save(business);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CrmBusiness business) {
        businessService.update(business);
        return Result.ok();
    }

    @PutMapping("/stage/{id}")
    public Result<Void> updateStage(@PathVariable Long id, @RequestParam String stage) {
        businessService.updateStage(id, stage);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        businessService.delete(id);
        return Result.ok();
    }

    @PostMapping("/convert/{id}")
    public Result<CrmContract> convertToContract(@PathVariable Long id) {
        return Result.ok(businessService.convertToContract(id));
    }
}
