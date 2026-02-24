package com.ihsan.aidatflow.controller;

import com.ihsan.aidatflow.dto.CreateDueRequest;
import com.ihsan.aidatflow.dto.CreatePaymentRequest;
import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.Payment;
import com.ihsan.aidatflow.service.DueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dues")
public class DueController {
    private final DueService dueService;

    public DueController(DueService dueService) {
        this.dueService = dueService;
    }

    @PostMapping
    public Due createDue(@Valid @RequestBody CreateDueRequest request) {
        return dueService.createDue(request);
    }

    @GetMapping("/apartment/{apartmentId}")
    public List<Due> listApartmentDues(@PathVariable Long apartmentId) {
        return dueService.listApartmentDues(apartmentId);
    }

    @PostMapping("/{dueId}/payments")
    public Payment createPayment(@PathVariable Long dueId, @Valid @RequestBody CreatePaymentRequest request) {
        return dueService.registerPayment(dueId, request);
    }
}
