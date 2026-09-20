package com.edusphere.fees;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeePaymentRepository extends JpaRepository<FeePayment, UUID> {
    boolean existsByPaymentReference(String paymentReference);
    Page<FeePayment> findByInvoiceId(UUID invoiceId, Pageable pageable);
}
