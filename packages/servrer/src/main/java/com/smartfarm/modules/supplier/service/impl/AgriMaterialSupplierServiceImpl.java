package com.smartfarm.modules.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.supplier.dto.SupplierSaveRequest;
import com.smartfarm.modules.supplier.entity.AgriMaterialSupplier;
import com.smartfarm.modules.supplier.mapper.AgriMaterialSupplierMapper;
import com.smartfarm.modules.supplier.service.AgriMaterialSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AgriMaterialSupplierServiceImpl implements AgriMaterialSupplierService {

    private final AgriMaterialSupplierMapper supplierMapper;

    @Override
    public IPage<AgriMaterialSupplier> pageSuppliers(long pageNum, long pageSize, String keyword, String cooperationStatus) {
        LambdaQueryWrapper<AgriMaterialSupplier> wrapper = new LambdaQueryWrapper<AgriMaterialSupplier>()
                .and(StringUtils.hasText(keyword), q -> q
                        .like(AgriMaterialSupplier::getSupplierName, keyword)
                        .or()
                        .like(AgriMaterialSupplier::getContactPerson, keyword))
                .eq(StringUtils.hasText(cooperationStatus), AgriMaterialSupplier::getCooperationStatus, cooperationStatus)
                .orderByDesc(AgriMaterialSupplier::getCreatedAt);
        return supplierMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public AgriMaterialSupplier getSupplierById(Long supplierId) {
        AgriMaterialSupplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) {
            throw new BusinessException(404, "Supplier not found");
        }
        return supplier;
    }

    @Override
    public void createSupplier(SupplierSaveRequest request) {
        checkDuplicateName(null, request.getSupplierName());
        AgriMaterialSupplier supplier = new AgriMaterialSupplier();
        BeanUtils.copyProperties(request, supplier);
        supplierMapper.insert(supplier);
    }

    @Override
    public void updateSupplier(Long supplierId, SupplierSaveRequest request) {
        AgriMaterialSupplier existing = getSupplierById(supplierId);
        checkDuplicateName(supplierId, request.getSupplierName());
        BeanUtils.copyProperties(request, existing);
        supplierMapper.updateById(existing);
    }

    @Override
    public void deleteSupplier(Long supplierId) {
        getSupplierById(supplierId);
        supplierMapper.deleteById(supplierId);
    }

    private void checkDuplicateName(Long supplierId, String supplierName) {
        AgriMaterialSupplier supplier = supplierMapper.selectOne(new LambdaQueryWrapper<AgriMaterialSupplier>()
                .eq(AgriMaterialSupplier::getSupplierName, supplierName)
                .ne(supplierId != null, AgriMaterialSupplier::getSupplierId, supplierId)
                .last("LIMIT 1"));
        if (supplier != null) {
            throw new BusinessException("Supplier name already exists");
        }
    }
}
