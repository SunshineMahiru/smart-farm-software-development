package com.smartfarm.modules.supplier.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.supplier.dto.SupplierSaveRequest;
import com.smartfarm.modules.supplier.entity.AgriMaterialSupplier;

public interface AgriMaterialSupplierService {

    IPage<AgriMaterialSupplier> pageSuppliers(long pageNum, long pageSize, String keyword, String cooperationStatus);

    AgriMaterialSupplier getSupplierById(Long supplierId);

    void createSupplier(SupplierSaveRequest request);

    void updateSupplier(Long supplierId, SupplierSaveRequest request);

    void deleteSupplier(Long supplierId);
}
