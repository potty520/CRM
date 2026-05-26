package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmContract;
import com.crm.entity.CrmInvoice;
import com.crm.entity.CrmPayment;
import com.crm.service.CrmContractService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
public class CrmContractController {

    private final CrmContractService contractService;

    public CrmContractController(CrmContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/page")
    public Result<PageResult<CrmContract>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String contractName,
            @RequestParam(required = false) String status) {
        return Result.ok(contractService.page(pageNum, pageSize, contractName, status));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CrmContract contract) {
        contractService.save(contract);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CrmContract contract) {
        contractService.update(contract);
        return Result.ok();
    }

    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam String status) {
        contractService.approve(id, status);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return Result.ok();
    }

    @PostMapping("/payment")
    public Result<Void> addPayment(@RequestBody CrmPayment payment) {
        contractService.addPayment(payment);
        return Result.ok();
    }

    @GetMapping("/payment/{contractId}")
    public Result<List<CrmPayment>> listPayments(@PathVariable Long contractId) {
        return Result.ok(contractService.listPayments(contractId));
    }

    @PostMapping("/invoice")
    public Result<Void> addInvoice(@RequestBody CrmInvoice invoice) {
        contractService.addInvoice(invoice);
        return Result.ok();
    }

    @GetMapping("/invoice/{contractId}")
    public Result<List<CrmInvoice>> listInvoices(@PathVariable Long contractId) {
        return Result.ok(contractService.listInvoices(contractId));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                      @RequestParam(required = false) String contractName) throws Exception {
        contractService.export(response, contractName);
    }

    @PostMapping("/import")
    public Result<Map<String, Integer>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        int count = contractService.importExcel(file);
        Map<String, Integer> data = new HashMap<>();
        data.put("count", count);
        return Result.ok(data);
    }
}
