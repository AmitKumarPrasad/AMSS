package com.edusphere.database;

import com.edusphere.academics.AcademicYear;
import com.edusphere.academics.AcademicYearRepository;
import com.edusphere.academics.Subject;
import com.edusphere.academics.SubjectRepository;
import com.edusphere.staff.StaffMember;
import com.edusphere.staff.StaffRepository;
import com.edusphere.attendance.AttendanceRecord;
import com.edusphere.attendance.AttendanceRepository;
import com.edusphere.fees.FeeInvoice;
import com.edusphere.fees.FeeInvoiceRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import java.math.BigDecimal;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RepositoryQueryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void emptyAttendanceAndUnfilteredAcademicsWorkOnPostgres() throws Exception {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .applySetting("hibernate.connection.username", POSTGRES.getUsername())
                .applySetting("hibernate.connection.password", POSTGRES.getPassword())
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .build();
        try (SessionFactory factory = new MetadataSources(registry)
                .addAnnotatedClass(AttendanceRecord.class).addAnnotatedClass(AcademicYear.class)
                .addAnnotatedClass(StaffMember.class).addAnnotatedClass(Subject.class).addAnnotatedClass(FeeInvoice.class).buildMetadata().buildSessionFactory();
             var session = factory.openSession()) {
            UUID school = UUID.randomUUID();
            session.beginTransaction();
            session.persist(new StaffMember(school, "STAFF-1", "Amit Teacher", null, null, "Teacher", "FULL_TIME", LocalDate.now()));
            session.persist(new Subject(school, "MATH", "Mathematics"));
            session.persist(new AcademicYear(school, "2026", "2026-27", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31)));
            FeeInvoice invoice = new FeeInvoice(school, UUID.randomUUID(), "QA-1", LocalDate.now(), BigDecimal.ONE);
            session.persist(invoice);
            session.getTransaction().commit();

            String summaryHql = AttendanceRepository.class.getMethod("summary", UUID.class, LocalDate.class)
                    .getAnnotation(Query.class).value();
            var summary = session.createQuery(summaryHql, AttendanceRepository.SummaryCounts.class)
                    .setParameter("schoolId", school).setParameter("date", LocalDate.of(2026, 10, 14)).getSingleResult();
            assertEquals(new AttendanceRepository.SummaryCounts(0, 0, 0, 0, 0), summary);

            String subjectHql = SubjectRepository.class.getMethod("search", UUID.class, String.class, Pageable.class)
                    .getAnnotation(Query.class).value();
            var subjects = session.createQuery(subjectHql, Subject.class).setParameter("schoolId", school)
                    .setParameter("query", null).getResultList();
            assertEquals(1, subjects.size());
            assertEquals("MATH", subjects.get(0).getCode());
            assertTrue(session.createQuery(subjectHql, Subject.class).setParameter("schoolId", UUID.randomUUID())
                    .setParameter("query", null).getResultList().isEmpty());

            String yearHql = AcademicYearRepository.class.getMethod("search", UUID.class, String.class, Pageable.class)
                    .getAnnotation(Query.class).value();
            assertEquals(1, session.createQuery(yearHql, AcademicYear.class).setParameter("schoolId", school)
                    .setParameter("status", null).getResultList().size());

            String staffHql = StaffRepository.class.getMethod("search", UUID.class, String.class, String.class, Pageable.class)
                    .getAnnotation(Query.class).value();
            assertEquals(1, session.createQuery(staffHql, StaffMember.class).setParameter("schoolId", school)
                    .setParameter("status", "ACTIVE").setParameter("query", null).getResultList().size());
            assertEquals(1, session.createQuery(staffHql, StaffMember.class).setParameter("schoolId", school)
                    .setParameter("status", "ACTIVE").setParameter("query", "amit").getResultList().size());

            session.clear();
            session.doWork(connection -> connection.setReadOnly(true));
            session.beginTransaction();
            FeeInvoiceRepository invoices = new JpaRepositoryFactory(session).getRepository(FeeInvoiceRepository.class);
            assertTrue(invoices.findById(invoice.getId()).isPresent());
            session.getTransaction().commit();
            session.doWork(connection -> connection.setReadOnly(false));
            session.beginTransaction();
            assertTrue(invoices.findByIdForUpdate(invoice.getId()).isPresent());
            session.getTransaction().commit();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
