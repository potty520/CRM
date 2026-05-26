package com.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.ErpFinancial;
import com.crm.entity.ErpInventory;
import com.crm.entity.ErpProduct;
import com.crm.mapper.ErpFinancialMapper;
import com.crm.mapper.ErpInventoryMapper;
import com.crm.mapper.ErpProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/erp")
public class ErpController {

    @Autowired
    private ErpProductMapper productMapper;

    @Autowired
    private ErpInventoryMapper inventoryMapper;

    @Autowired
    private ErpFinancialMapper financialMapper;

    // ============== 产品管理 ==============
    @PostMapping("/product")
    public Result<?> addProduct(@RequestBody ErpProduct product) {
        productMapper.insert(product);
        return Result.ok();
    }

    @PutMapping("/product/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody ErpProduct product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.ok();
    }

    @DeleteMapping("/product/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/product/{id}")
    public Result<ErpProduct> getProduct(@PathVariable Long id) {
        return Result.ok(productMapper.selectById(id));
    }

    @GetMapping("/product/page")
    public Result<PageResult<ErpProduct>> productPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ErpProduct> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(ErpProduct::getProductName, keyword).or()
              .like(ErpProduct::getProductCode, keyword);
        }
        Page<ErpProduct> page = new Page<>(pageNum, pageSize);
        Page<ErpProduct> result = productMapper.selectPage(page, w);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/product/export")
    public Result<List<ErpProduct>> exportProduct() {
        return Result.ok(productMapper.selectList(null));
    }

    // ============== 库存管理 ==============
    @PostMapping("/inventory")
    public Result<?> addInventory(@RequestBody ErpInventory inventory) {
        inventory.setLastInbound(LocalDateTime.now());
        inventoryMapper.insert(inventory);
        return Result.ok();
    }

    @PutMapping("/inventory/{id}")
    public Result<?> updateInventory(@PathVariable Long id, @RequestBody ErpInventory inventory) {
        inventory.setId(id);
        inventoryMapper.updateById(inventory);
        return Result.ok();
    }

    @DeleteMapping("/inventory/{id}")
    public Result<?> deleteInventory(@PathVariable Long id) {
        inventoryMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/inventory/page")
    public Result<PageResult<ErpInventory>> inventoryPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ErpInventory> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(ErpInventory::getProductName, keyword).or()
              .like(ErpInventory::getWarehouseName, keyword);
        }
        Page<ErpInventory> page = new Page<>(pageNum, pageSize);
        Page<ErpInventory> result = inventoryMapper.selectPage(page, w);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    // ============== 财务管理 ==============
    @PostMapping("/financial")
    public Result<?> addFinancial(@RequestBody ErpFinancial financial) {
        financialMapper.insert(financial);
        return Result.ok();
    }

    @PutMapping("/financial/{id}")
    public Result<?> updateFinancial(@PathVariable Long id, @RequestBody ErpFinancial financial) {
        financial.setId(id);
        financialMapper.updateById(financial);
        return Result.ok();
    }

    @DeleteMapping("/financial/{id}")
    public Result<?> deleteFinancial(@PathVariable Long id) {
        financialMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/financial/page")
    public Result<PageResult<ErpFinancial>> financialPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<ErpFinancial> w = new LambdaQueryWrapper<>();
        if (startDate != null && !startDate.isBlank()) {
            w.ge(ErpFinancial::getRecordDate, startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            w.le(ErpFinancial::getRecordDate, endDate);
        }
        if (type != null) {
            w.eq(ErpFinancial::getType, type);
        }
        w.orderByDesc(ErpFinancial::getRecordDate);
        Page<ErpFinancial> page = new Page<>(pageNum, pageSize);
        Page<ErpFinancial> result = financialMapper.selectPage(page, w);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}