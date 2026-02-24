package com.ihsan.aidatflow.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal totalDueAmount,
        BigDecimal totalCollected,
        BigDecimal totalOutstanding,
        long dueCount,
        long paidCount,
        long unpaidCount
) {}
