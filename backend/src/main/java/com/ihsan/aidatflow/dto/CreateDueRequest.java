package com.ihsan.aidatflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDueRequest(
        @NotNull Long apartmentId,
        @NotBlank String period,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate dueDate
) {}
