package com.smartfarm.modules.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agri_material_supplier")
public class AgriMaterialSupplier {

    @TableId(value = "supplier_id", type = IdType.AUTO)
    private Long supplierId;

    private String supplierName;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private String cooperationStatus;

    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
