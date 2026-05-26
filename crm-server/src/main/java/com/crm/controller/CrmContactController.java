package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmContact;
import com.crm.service.CrmContactService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class CrmContactController {

    private final CrmContactService contactService;

    public CrmContactController(CrmContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/page")
    public Result<PageResult<CrmContact>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) Long customerId) {
        return Result.ok(contactService.page(pageNum, pageSize, contactName, customerId));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<CrmContact>> listByCustomer(@PathVariable Long customerId) {
        return Result.ok(contactService.listByCustomer(customerId));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CrmContact contact) {
        contactService.save(contact);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CrmContact contact) {
        contactService.update(contact);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return Result.ok();
    }
}
