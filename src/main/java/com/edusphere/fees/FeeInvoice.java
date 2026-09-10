package com.edusphere.fees;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fee_invoices")
public class FeeInvoice {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(name = "student_id", nullable = false) private UUID studentId;
    @Column(name = "invoice_number", nullable = false, length = 64) private String invoiceNumber;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2) private BigDecimal paidAmount = BigDecimal.ZERO;
    @Column(nullable = false, length = 32) private String status = "OPEN";

    protected FeeInvoice() {}
    public FeeInvoice(UUID schoolId, UUID studentId, String invoiceNumber, LocalDate dueDate, BigDecimal amount) {
        this.schoolId = schoolId; this.studentId = studentId; this.invoiceNumber = invoiceNumber;
        this.dueDate = dueDate; this.amount = amount;
    }
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getStudentId() { return studentId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public String getStatus() { return status; }
    public void applyPayment(BigDecimal payment) {
        this.paidAmount = this.paidAmount.add(payment);
        this.status = this.paidAmount.compareTo(this.amount) >= 0 ? "PAID" : "PARTIALLY_PAID";
    }
}
