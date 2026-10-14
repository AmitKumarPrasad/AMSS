package com.edusphere.fees;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, UUID> {
    boolean existsBySchoolIdAndInvoiceNumber(UUID schoolId, String invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from FeeInvoice i where i.id = :id")
    java.util.Optional<FeeInvoice> findByIdForUpdate(@Param("id") UUID id);

    Page<FeeInvoice> findBySchoolIdAndStudentIdOrderByDueDateDesc(UUID schoolId, UUID studentId, Pageable pageable);

    Page<FeeInvoice> findBySchoolIdOrderByDueDateDesc(UUID schoolId, Pageable pageable);

    Page<FeeInvoice> findBySchoolIdAndStatusOrderByDueDateDesc(UUID schoolId, String status, Pageable pageable);
}
