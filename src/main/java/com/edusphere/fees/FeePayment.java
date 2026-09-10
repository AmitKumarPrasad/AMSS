package com.edusphere.fees;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fee_payments")
public class FeePayment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    @Column(name = "payment_reference", nullable = false, unique = true, length = 100) private String paymentReference;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "paid_at", nullable = false) private Instant paidAt = Instant.now();
    @Column(nullable = false, length = 32) private String status = "SUCCESS";

    protected FeePayment() {}
    public FeePayment(UUID invoiceId, String paymentReference, BigDecimal amount) {
        this.invoiceId = invoiceId; this.paymentReference = paymentReference; this.amount = amount;
    }
    public UUID getId() { return id; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getAmount() { return amount; }
    public Instant getPaidAt() { return paidAt; }
    public String getStatus() { return status; }
}
