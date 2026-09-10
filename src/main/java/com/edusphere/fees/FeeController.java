package com.edusphere.fees;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/fees")
public class FeeController {
    private final FeeService feeService;
    private final TenantAccess tenantAccess;

    public FeeController(FeeService feeService, TenantAccess tenantAccess) {
        this.feeService = feeService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public FeeService.InvoiceView createInvoice(@PathVariable UUID schoolId, @Valid @RequestBody CreateInvoiceRequest request,
                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return feeService.createInvoice(schoolId, new FeeService.CreateInvoiceRequest(
                request.studentId(), request.invoiceNumber(), request.dueDate(), request.amount()));
    }

    @GetMapping("/students/{studentId}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT','TEACHER')")
    public List<FeeService.InvoiceView> listInvoices(@PathVariable UUID schoolId, @PathVariable UUID studentId,
                                                      Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return feeService.listInvoices(schoolId, studentId);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public FeeService.PaymentView recordPayment(@PathVariable UUID schoolId, @PathVariable UUID invoiceId,
                                                 @Valid @RequestBody RecordPaymentRequest request,
                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return feeService.recordPayment(schoolId, invoiceId,
                new FeeService.RecordPaymentRequest(request.paymentReference(), request.amount()));
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT','TEACHER')")
    public List<FeeService.PaymentView> listPayments(@PathVariable UUID schoolId, @PathVariable UUID invoiceId,
                                                      Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return feeService.listPayments(schoolId, invoiceId);
    }

    public record CreateInvoiceRequest(@NotNull UUID studentId, @NotBlank String invoiceNumber,
                                        @NotNull LocalDate dueDate, @NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record RecordPaymentRequest(@NotBlank String paymentReference,
                                       @NotNull @DecimalMin("0.01") BigDecimal amount) {}
}
