package com.edusphere.fees;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, UUID> {
    boolean existsBySchoolIdAndInvoiceNumber(UUID schoolId, String invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<FeeInvoice> findById(UUID id);

    List<FeeInvoice> findBySchoolIdAndStudentIdOrderByDueDateDesc(UUID schoolId, UUID studentId);
}
