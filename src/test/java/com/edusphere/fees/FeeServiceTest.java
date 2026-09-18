package com.edusphere.fees;

import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    @Mock FeeInvoiceRepository invoices;
    @Mock FeePaymentRepository payments;
    @Mock StudentRepository students;

    @Test void rejectsBlankInvoiceNumber() {
        FeeService service = new FeeService(invoices, payments, students);
        assertThrows(ResponseStatusException.class, () -> service.createInvoice(UUID.randomUUID(),
                new FeeService.CreateInvoiceRequest(UUID.randomUUID(), "  ", LocalDate.now(), BigDecimal.TEN)));
        verifyNoInteractions(students, invoices);
    }

    @Test void rejectsPaymentOverOutstandingBalance() {
        UUID school = UUID.randomUUID(), studentId = UUID.randomUUID(), invoiceId = UUID.randomUUID();
        FeeInvoice invoice = new FeeInvoice(school, studentId, "INV-1", LocalDate.now(), BigDecimal.TEN);
        FeeService service = new FeeService(invoices, payments, students);
        when(invoices.findById(invoiceId)).thenReturn(Optional.of(invoice));
        assertThrows(ResponseStatusException.class, () -> service.recordPayment(school, invoiceId,
                new FeeService.RecordPaymentRequest("PAY-1", BigDecimal.valueOf(11))));
        verify(payments, never()).save(any());
    }

    @Test void recordsFullPaymentAndMarksInvoicePaid() {
        UUID school = UUID.randomUUID(), studentId = UUID.randomUUID(), invoiceId = UUID.randomUUID();
        FeeInvoice invoice = new FeeInvoice(school, studentId, "INV-1", LocalDate.now(), BigDecimal.TEN);
        FeeService service = new FeeService(invoices, payments, students);
        when(invoices.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(payments.existsByPaymentReference("PAY-1")).thenReturn(false);
        when(payments.save(any())).thenAnswer(i -> i.getArgument(0));
        service.recordPayment(school, invoiceId, new FeeService.RecordPaymentRequest(" PAY-1 ", BigDecimal.TEN));
        assertEquals("PAID", invoice.getStatus());
        assertEquals(BigDecimal.TEN, invoice.getPaidAmount());
        verify(invoices).save(invoice);
    }
}
