package com.ihsan.aidatflow.repository;

import com.ihsan.aidatflow.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByDueIdOrderByPaidAtDesc(Long dueId);

    @Query("select coalesce(sum(p.amount),0) from Payment p where p.dueId = :dueId")
    BigDecimal totalPaidForDue(Long dueId);
}
