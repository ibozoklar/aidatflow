package com.ihsan.aidatflow.service;

import com.ihsan.aidatflow.dto.CreateDueRequest;
import com.ihsan.aidatflow.dto.CreatePaymentRequest;
import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.DueStatus;
import com.ihsan.aidatflow.entity.Payment;
import com.ihsan.aidatflow.entity.PaymentMethod;
import com.ihsan.aidatflow.repository.DueRepository;
import com.ihsan.aidatflow.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DueServiceTest {

    @Mock DueRepository dueRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks DueService dueService;

    @Test
    void createDue_setsUnpaidStatus() {
        CreateDueRequest request = new CreateDueRequest(1L, "2026-02", new BigDecimal("1200.00"), LocalDate.now().plusDays(10));

        Due persisted = new Due();
        persisted.setId(10L);
        persisted.setStatus(DueStatus.UNPAID);
        when(dueRepository.save(any(Due.class))).thenReturn(persisted);

        Due result = dueService.createDue(request);

        assertThat(result.getId()).isEqualTo(10L);
        ArgumentCaptor<Due> captor = ArgumentCaptor.forClass(Due.class);
        verify(dueRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DueStatus.UNPAID);
    }

    @Test
    void registerPayment_marksDuePartial_whenTotalBelowAmount() {
        Due due = new Due();
        due.setId(5L);
        due.setAmount(new BigDecimal("1000.00"));
        due.setStatus(DueStatus.UNPAID);

        when(dueRepository.findById(5L)).thenReturn(Optional.of(due));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.totalPaidForDue(5L)).thenReturn(new BigDecimal("300.00"));

        dueService.registerPayment(5L, new CreatePaymentRequest(new BigDecimal("300.00"), PaymentMethod.BANK_TRANSFER, "ilk ödeme"));

        assertThat(due.getStatus()).isEqualTo(DueStatus.PARTIAL);
        verify(dueRepository).save(due);
    }

    @Test
    void registerPayment_marksDuePaid_whenTotalReachesAmount() {
        Due due = new Due();
        due.setId(6L);
        due.setAmount(new BigDecimal("1000.00"));
        due.setStatus(DueStatus.PARTIAL);

        when(dueRepository.findById(6L)).thenReturn(Optional.of(due));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.totalPaidForDue(6L)).thenReturn(new BigDecimal("1000.00"));

        dueService.registerPayment(6L, new CreatePaymentRequest(new BigDecimal("700.00"), PaymentMethod.CASH, "kalan"));

        assertThat(due.getStatus()).isEqualTo(DueStatus.PAID);
        verify(dueRepository).save(due);
    }
}
