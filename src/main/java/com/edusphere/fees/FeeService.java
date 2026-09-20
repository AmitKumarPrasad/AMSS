package com.edusphere.fees;

import com.edusphere.student.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FeeService {
    private static final int MAX_PAGE_SIZE = 100;

    private final FeeInvoiceRepository invoiceRepository;
    private final FeePaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    public FeeService(FeeInvoiceRepository invoiceRepository, FeePaymentRepository paymentRepository,
                      StudentRepository studentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
    }

    public InvoiceView createInvoice(UUID schoolId, CreateInvoiceRequest request) {
        if (request == null || request.studentId() == null || request.invoiceNumber() == null
                || request.dueDate() == null || request.amount() == null) {
            bad("studentId, invoiceNumber, dueDate and amount are required");
        }
        String number = requiredText(request.invoiceNumber(), "invoiceNumber");
        requireActiveStudent(schoolId, request.studentId());
        if (request.amount().signum() <= 0) bad("amount must be greater than zero");
        if (invoiceRepository.existsBySchoolIdAndInvoiceNumber(schoolId, number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice number already exists");
        }
        return toView(invoiceRepository.save(new FeeInvoice(schoolId, request.studentId(), number,
                request.dueDate(), request.amount())));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceView> listSchoolInvoices(UUID schoolId, int page, int size, String status) {
        if (page < 0) bad("page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) bad("size must be between 1 and " + MAX_PAGE_SIZE);
        String normalizedStatus = status == null ? null : status.trim().toUpperCase();
        if (normalizedStatus != null && !normalizedStatus.isBlank()
                && !List.of("OPEN", "PARTIALLY_PAID", "PAID").contains(normalizedStatus)) {
            bad("status must be OPEN, PARTIALLY_PAID or PAID");
        }
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dueDate"));
        Page<FeeInvoice> invoices = normalizedStatus == null || normalizedStatus.isBlank()
                ? invoiceRepository.findBySchoolIdOrderByDueDateDesc(schoolId, request)
                : invoiceRepository.findBySchoolIdAndStatusOrderByDueDateDesc(schoolId, normalizedStatus, request);
        return invoices.map(this::toView);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceView> listInvoices(UUID schoolId, UUID studentId, int page, int size) {
        requireStudent(schoolId, studentId);
        if (page < 0) bad("page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) bad("size must be between 1 and " + MAX_PAGE_SIZE);
        PageRequest request = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "dueDate").and(Sort.by(Sort.Direction.ASC, "id")));
        return invoiceRepository.findBySchoolIdAndStudentIdOrderByDueDateDesc(schoolId, studentId, request)
                .map(this::toView);
    }

    @Transactional
    public PaymentView recordPayment(UUID schoolId, UUID invoiceId, RecordPaymentRequest request) {
        if (request == null || request.paymentReference() == null || request.amount() == null) {
            bad("paymentReference and amount are required");
        }
        FeeInvoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> schoolId.equals(i.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (request.amount().signum() <= 0) bad("amount must be greater than zero");
        if (request.amount().compareTo(invoice.getAmount().subtract(invoice.getPaidAmount())) > 0) {
            bad("payment exceeds outstanding balance");
        }
        String reference = requiredText(request.paymentReference(), "paymentReference");
        if (paymentRepository.existsByPaymentReference(reference)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment reference already exists");
        }
        FeePayment payment = paymentRepository.save(new FeePayment(invoiceId, reference, request.amount()));
        invoice.applyPayment(request.amount());
        invoiceRepository.save(invoice);
        return toView(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentView> listPayments(UUID schoolId, UUID invoiceId, int page, int size) {
        if (page < 0) bad("page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) bad("size must be between 1 and " + MAX_PAGE_SIZE);
        FeeInvoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> schoolId.equals(i.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        PageRequest request = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "paidAt").and(Sort.by(Sort.Direction.ASC, "id")));
        return paymentRepository.findByInvoiceId(invoice.getId(), request).map(this::toView);
    }

    private void requireStudent(UUID schoolId, UUID studentId) {
        studentRepository.findById(studentId).filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }
    private void requireActiveStudent(UUID schoolId, UUID studentId) {
        var student = studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!"ACTIVE".equals(student.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fees can only be created for an active student");
        }
    }
    private String requiredText(String value, String field) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isBlank()) bad(field + " is required");
        return normalized;
    }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private InvoiceView toView(FeeInvoice i) {
        return new InvoiceView(i.getId(), i.getSchoolId(), i.getStudentId(), i.getInvoiceNumber(),
                i.getDueDate(), i.getAmount(), i.getPaidAmount(), i.getStatus());
    }
    private PaymentView toView(FeePayment p) {
        return new PaymentView(p.getId(), p.getInvoiceId(), p.getPaymentReference(), p.getAmount(), p.getPaidAt(), p.getStatus());
    }

    public record CreateInvoiceRequest(UUID studentId, String invoiceNumber, LocalDate dueDate, BigDecimal amount) {}
    public record RecordPaymentRequest(String paymentReference, BigDecimal amount) {}
    public record InvoiceView(UUID id, UUID schoolId, UUID studentId, String invoiceNumber, LocalDate dueDate,
                              BigDecimal amount, BigDecimal paidAmount, String status) {}
    public record PaymentView(UUID id, UUID invoiceId, String paymentReference, BigDecimal amount,
                              java.time.Instant paidAt, String status) {}
}
