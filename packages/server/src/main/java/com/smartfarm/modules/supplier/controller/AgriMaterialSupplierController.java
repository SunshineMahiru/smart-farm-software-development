package com.smartfarm.modules.supplier.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.supplier.dto.SupplierSaveRequest;
import com.smartfarm.modules.supplier.entity.AgriMaterialSupplier;
import com.smartfarm.modules.supplier.service.AgriMaterialSupplierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/suppliers")
public class AgriMaterialSupplierController {

    private final AgriMaterialSupplierService supplierService;

    @GetMapping
    public Result<IPage<AgriMaterialSupplier>> pageSuppliers(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cooperationStatus) {
        return Result.success(supplierService.pageSuppliers(pageNum, pageSize, keyword, cooperationStatus));
    }

    @GetMapping("/{supplierId}")
    public Result<AgriMaterialSupplier> getSupplier(@PathVariable Long supplierId) {
        return Result.success(supplierService.getSupplierById(supplierId));
    }

    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createSupplier(@Valid @RequestBody SupplierSaveRequest request) {
        supplierService.createSupplier(request);
        return Result.success("Supplier created successfully");
    }

    @SaCheckRole("管理员")
    @PutMapping("/{supplierId}")
    public Result<String> updateSupplier(@PathVariable Long supplierId, @Valid @RequestBody SupplierSaveRequest request) {
        supplierService.updateSupplier(supplierId, request);
        return Result.success("Supplier updated successfully");
    }

    @SaCheckRole("管理员")
    @DeleteMapping("/{supplierId}")
    public Result<String> deleteSupplier(@PathVariable Long supplierId) {
        supplierService.deleteSupplier(supplierId);
        return Result.success("Supplier deleted successfully");
    }
}
