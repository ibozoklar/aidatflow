package com.ihsan.aidatflow.controller;

import com.ihsan.aidatflow.dto.DashboardSummaryResponse;
import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.DueStatus;
import com.ihsan.aidatflow.repository.PaymentRepository;
import com.ihsan.aidatflow.service.DueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DueService dueService;
    private final PaymentRepository paymentRepository;

    public DashboardController(DueService dueService, PaymentRepository paymentRepository) {
        this.dueService = dueService;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@RequestParam Long apartmentId) {
        List<Due> dues = dueService.listApartmentDues(apartmentId);

        BigDecimal totalDue = dues.stream()
                .map(Due::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = dues.stream()
                .map(d -> paymentRepository.totalPaidForDue(d.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = totalDue.subtract(totalCollected).max(BigDecimal.ZERO);

        long paidCount = dues.stream().filter(d -> d.getStatus() == DueStatus.PAID).count();
        long unpaidCount = dues.stream().filter(d -> d.getStatus() != DueStatus.PAID).count();

        return new DashboardSummaryResponse(
                totalDue,
                totalCollected,
                totalOutstanding,
                dues.size(),
                paidCount,
                unpaidCount
        );
    }
}
