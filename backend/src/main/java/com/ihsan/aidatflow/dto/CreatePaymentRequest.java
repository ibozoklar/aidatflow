package com.ihsan.aidatflow.dto;

import com.ihsan.aidatflow.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentMethod method,
        String note
) {}
