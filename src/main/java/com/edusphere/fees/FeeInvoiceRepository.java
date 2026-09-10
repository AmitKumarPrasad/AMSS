package com.edusphere.fees;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, UUID> {
    boolean existsBySchoolIdAndInvoiceNumber(UUID schoolId, String invoiceNumber);
    List<FeeInvoice> findBySchoolIdAndStudentIdOrderByDueDateDesc(UUID schoolId, UUID studentId);
}
