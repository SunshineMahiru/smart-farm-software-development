package com.smartfarm.modules.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierSaveRequest {

    @NotBlank(message = "Supplier name cannot be empty")
    @Size(max = 100, message = "Supplier name length cannot exceed 100")
    private String supplierName;

    @NotBlank(message = "Contact person cannot be empty")
    @Size(max = 50, message = "Contact person length cannot exceed 50")
    private String contactPerson;

    @NotBlank(message = "Contact phone cannot be empty")
    @Pattern(regexp = "^1\\d{10}$", message = "Phone format is invalid")
    private String contactPhone;

    @Size(max = 200, message = "Address length cannot exceed 200")
    private String address;

    @NotBlank(message = "Cooperation status cannot be empty")
    private String cooperationStatus;
}
