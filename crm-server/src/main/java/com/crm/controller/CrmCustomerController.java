package com.crm.controller;

import com.crm.aspect.OperLogAspect.OperLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmCustomer;
import com.crm.service.CrmCustomerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CrmCustomerController {

    private final CrmCustomerService customerService;

    public CrmCustomerController(CrmCustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/page")
    public Result<PageResult<CrmCustomer>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer poolStatus,
            @RequestParam(required = false) Long ownerId) {
        return Result.ok(customerService.page(pageNum, pageSize, customerName, poolStatus, ownerId));
    }

    @GetMapping("/{id}")
    public Result<CrmCustomer> getById(@PathVariable Long id) {
        return Result.ok(customerService.getById(id));
    }

    @PostMapping
    @OperLog(module = "客户管理", operation = "新增客户")
    public Result<Void> save(@RequestBody CrmCustomer customer) {
        customerService.save(customer);
        return Result.ok();
    }

    @PutMapping
    @OperLog(module = "客户管理", operation = "编辑客户")
    public Result<Void> update(@RequestBody CrmCustomer customer) {
        customerService.update(customer);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "客户管理", operation = "删除客户")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok();
    }

    @PutMapping("/assign")
    public Result<Void> assign(@RequestParam Long customerId, @RequestParam Long ownerId) {
        customerService.assign(customerId, ownerId);
        return Result.ok();
    }

    @PutMapping("/pool/{id}")
    public Result<Void> toPool(@PathVariable Long id) {
        customerService.toPool(id);
        return Result.ok();
    }

    @PutMapping("/claim/{id}")
    public Result<Void> claim(@PathVariable Long id) {
        customerService.claimFromPool(id);
        return Result.ok();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String customerName) throws Exception {
        customerService.export(response, customerName);
    }

    @PostMapping("/import")
    public Result<Map<String, Integer>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        int count = customerService.importExcel(file);
        Map<String, Integer> data = new HashMap<>();
        data.put("count", count);
        return Result.ok(data);
    }
}
