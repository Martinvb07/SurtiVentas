package com.surtiventas.backend.customer.dto;

import com.surtiventas.backend.customer.CustomerClassification;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CustomerCreateRequest(
        @NotBlank @Size(max = 150) String storeName,
        @NotBlank @Size(max = 150) String ownerName,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 255) String address,
        BigDecimal latitude,
        BigDecimal longitude,
        @NotNull @DecimalMin(value = "0.0") BigDecimal creditLimit,
        @NotNull CustomerClassification classification
) {
}
