package com.edusphere.fees;

import com.edusphere.student.StudentRepository;
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
        requireStudent(schoolId, request.studentId());
        if (request.amount().signum() <= 0) bad("amount must be greater than zero");
        String number = request.invoiceNumber().trim();
        if (invoiceRepository.existsBySchoolIdAndInvoiceNumber(schoolId, number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice number already exists");
        }
        return toView(invoiceRepository.save(new FeeInvoice(schoolId, request.studentId(), number,
                request.dueDate(), request.amount())));
    }

    @Transactional(readOnly = true)
    public List<InvoiceView> listInvoices(UUID schoolId, UUID studentId) {
        requireStudent(schoolId, studentId);
        return invoiceRepository.findBySchoolIdAndStudentIdOrderByDueDateDesc(schoolId, studentId)
                .stream().map(this::toView).toList();
    }

    @Transactional
    public PaymentView recordPayment(UUID schoolId, UUID invoiceId, RecordPaymentRequest request) {
        FeeInvoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> schoolId.equals(i.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (request.amount().signum() <= 0) bad("amount must be greater than zero");
        if (request.amount().compareTo(invoice.getAmount().subtract(invoice.getPaidAmount())) > 0) {
            bad("payment exceeds outstanding balance");
        }
        String reference = request.paymentReference().trim();
        if (paymentRepository.existsByPaymentReference(reference)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment reference already exists");
        }
        FeePayment payment = paymentRepository.save(new FeePayment(invoiceId, reference, request.amount()));
        invoice.applyPayment(request.amount());
        invoiceRepository.save(invoice);
        return toView(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentView> listPayments(UUID schoolId, UUID invoiceId) {
        FeeInvoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> schoolId.equals(i.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        return paymentRepository.findByInvoiceIdOrderByPaidAtDesc(invoice.getId()).stream().map(this::toView).toList();
    }

    private void requireStudent(UUID schoolId, UUID studentId) {
        studentRepository.findById(studentId).filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private InvoiceView toView(FeeInvoice i) { return new InvoiceView(i.getId(), i.getSchoolId(), i.getStudentId(), i.getInvoiceNumber(), i.getDueDate(), i.getAmount(), i.getPaidAmount(), i.getStatus()); }
    private PaymentView toView(FeePayment p) { return new PaymentView(p.getId(), p.getInvoiceId(), p.getPaymentReference(), p.getAmount(), p.getPaidAt(), p.getStatus()); }

    public record CreateInvoiceRequest(UUID studentId, String invoiceNumber, LocalDate dueDate, BigDecimal amount) {}
    public record RecordPaymentRequest(String paymentReference, BigDecimal amount) {}
    public record InvoiceView(UUID id, UUID schoolId, UUID studentId, String invoiceNumber, LocalDate dueDate, BigDecimal amount, BigDecimal paidAmount, String status) {}
    public record PaymentView(UUID id, UUID invoiceId, String paymentReference, BigDecimal amount, java.time.Instant paidAt, String status) {}
}
