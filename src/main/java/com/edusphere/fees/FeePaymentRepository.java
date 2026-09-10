package com.edusphere.fees;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeePaymentRepository extends JpaRepository<FeePayment, UUID> {
    boolean existsByPaymentReference(String paymentReference);
    List<FeePayment> findByInvoiceIdOrderByPaidAtDesc(UUID invoiceId);
}
