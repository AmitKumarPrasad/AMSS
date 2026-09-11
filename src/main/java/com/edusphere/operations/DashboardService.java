package com.edusphere.operations;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public DashboardView summary(UUID schoolId) {
        LocalDate today = LocalDate.now();
        long students = count("SELECT COUNT(*) FROM students WHERE school_id=? AND status='ACTIVE'", schoolId);
        long staff = count("SELECT COUNT(*) FROM staff_members WHERE school_id=? AND status='ACTIVE'", schoolId);
        long presentToday = count("SELECT COUNT(*) FROM attendance_records WHERE school_id=? AND attendance_date=? AND status='PRESENT'", schoolId, today);
        long absentToday = count("SELECT COUNT(*) FROM attendance_records WHERE school_id=? AND attendance_date=? AND status='ABSENT'", schoolId, today);
        long pendingLeaves = count("SELECT COUNT(*) FROM leave_requests WHERE school_id=? AND status='PENDING'", schoolId);
        long unreadNotifications = count("SELECT COUNT(*) FROM notifications WHERE school_id=? AND read_at IS NULL", schoolId);
        BigDecimal outstandingFees = jdbc.queryForObject(
                "SELECT COALESCE(SUM(GREATEST(amount - paid_amount, 0)), 0) FROM fee_invoices WHERE school_id=? AND status <> 'PAID'",
                BigDecimal.class, schoolId);
        return new DashboardView(schoolId, today, students, staff, presentToday, absentToday,
                pendingLeaves, unreadNotifications, outstandingFees == null ? BigDecimal.ZERO : outstandingFees);
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    public record DashboardView(UUID schoolId, LocalDate date, long activeStudents, long activeStaff,
                                long presentToday, long absentToday, long pendingLeaves,
                                long unreadNotifications, BigDecimal outstandingFees) {}
}
