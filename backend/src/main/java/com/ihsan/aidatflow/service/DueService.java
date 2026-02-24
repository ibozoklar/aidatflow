package com.ihsan.aidatflow.service;

import com.ihsan.aidatflow.dto.CreateDueRequest;
import com.ihsan.aidatflow.dto.CreatePaymentRequest;
import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.DueStatus;
import com.ihsan.aidatflow.entity.Payment;
import com.ihsan.aidatflow.repository.DueRepository;
import com.ihsan.aidatflow.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DueService {
    private final DueRepository dueRepository;
    private final PaymentRepository paymentRepository;

    public DueService(DueRepository dueRepository, PaymentRepository paymentRepository) {
        this.dueRepository = dueRepository;
        this.paymentRepository = paymentRepository;
    }

    public Due createDue(CreateDueRequest request) {
        Due due = new Due();
        due.setApartmentId(request.apartmentId());
        due.setPeriod(request.period());
        due.setAmount(request.amount());
        due.setDueDate(request.dueDate());
        due.setStatus(DueStatus.UNPAID);
        return dueRepository.save(due);
    }

    public List<Due> listApartmentDues(Long apartmentId) {
        return dueRepository.findByApartmentIdOrderByDueDateDesc(apartmentId);
    }

    public List<Payment> listPayments(Long dueId) {
        return paymentRepository.findByDueIdOrderByPaidAtDesc(dueId);
    }

    @Transactional
    public Payment registerPayment(Long dueId, CreatePaymentRequest request) {
        Due due = dueRepository.findById(dueId)
                .orElseThrow(() -> new EntityNotFoundException("Due not found: " + dueId));

        Payment payment = new Payment();
        payment.setDueId(dueId);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setNote(request.note());
        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal totalPaid = paymentRepository.totalPaidForDue(dueId);
        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            due.setStatus(DueStatus.UNPAID);
        } else if (totalPaid.compareTo(due.getAmount()) < 0) {
            due.setStatus(DueStatus.PARTIAL);
        } else {
            due.setStatus(DueStatus.PAID);
        }
        dueRepository.save(due);

        return savedPayment;
    }
}
