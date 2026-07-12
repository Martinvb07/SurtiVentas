package com.surtiventas.backend.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 150) String contactName,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 255) String address
) {
}
