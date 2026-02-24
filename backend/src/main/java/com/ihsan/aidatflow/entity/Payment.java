package com.ihsan.aidatflow.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "due_id", nullable = false)
    private Long dueId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Column(length = 255)
    private String note;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @PrePersist
    void prePersist() {
        if (paidAt == null) paidAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDueId() { return dueId; }
    public void setDueId(Long dueId) { this.dueId = dueId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
