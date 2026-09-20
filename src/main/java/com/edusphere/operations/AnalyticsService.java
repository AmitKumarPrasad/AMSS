package com.edusphere.operations;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {
    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 366;

    private final JdbcTemplate jdbc;

    public AnalyticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public AnalyticsView analytics(UUID schoolId, LocalDate from, LocalDate to) {
        if (schoolId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "schoolId is required");
        }

        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(DEFAULT_DAYS - 1L) : from;

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
        if (ChronoUnit.DAYS.between(start, end) + 1L > MAX_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "analytics range must not exceed 366 days");
        }

        AttendanceTotals attendance = attendanceTotals(schoolId, start, end);
        FeeTotals fees = feeTotals(schoolId, start, end);
        AssessmentTotals assessments = assessmentTotals(schoolId, start, end);

        return new AnalyticsView(
                schoolId,
                start,
                end,
                attendance,
                fees,
                assessments,
                attendanceTrend(schoolId, start, end),
                staffAttendanceTrend(schoolId, start, end),
                collectionTrend(schoolId, start, end)
        );
    }

    private AttendanceTotals attendanceTotals(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.queryForObject(
                """
                SELECT
                    COUNT(*) AS recorded,
                    COUNT(*) FILTER (WHERE status = 'PRESENT') AS present,
                    COUNT(*) FILTER (WHERE status = 'ABSENT') AS absent,
                    COUNT(*) FILTER (WHERE status = 'LATE') AS late,
                    COUNT(*) FILTER (WHERE status = 'EXCUSED') AS excused
                FROM attendance_records
                WHERE school_id = ?
                  AND attendance_date BETWEEN ? AND ?
                """,
                (rs, rowNum) -> new AttendanceTotals(
                        rs.getLong("recorded"),
                        rs.getLong("present"),
                        rs.getLong("absent"),
                        rs.getLong("late"),
                        rs.getLong("excused"),
                        percentage(rs.getLong("present"), rs.getLong("recorded"))
                ),
                schoolId, start, end
        );
    }

    private List<AttendancePoint> attendanceTrend(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.query(
                """
                SELECT attendance_date,
                       COUNT(*) AS recorded,
                       COUNT(*) FILTER (WHERE status = 'PRESENT') AS present,
                       COUNT(*) FILTER (WHERE status = 'ABSENT') AS absent,
                       COUNT(*) FILTER (WHERE status = 'LATE') AS late,
                       COUNT(*) FILTER (WHERE status = 'EXCUSED') AS excused
                FROM attendance_records
                WHERE school_id = ?
                  AND attendance_date BETWEEN ? AND ?
                GROUP BY attendance_date
                ORDER BY attendance_date
                """,
                (rs, rowNum) -> {
                    long recorded = rs.getLong("recorded");
                    long present = rs.getLong("present");
                    return new AttendancePoint(
                            rs.getObject("attendance_date", LocalDate.class),
                            recorded,
                            present,
                            rs.getLong("absent"),
                            rs.getLong("late"),
                            rs.getLong("excused"),
                            percentage(present, recorded)
                    );
                },
                schoolId, start, end
        );
    }

    private List<StaffAttendancePoint> staffAttendanceTrend(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.query(
                """
                SELECT attendance_date,
                       COUNT(*) AS recorded,
                       COUNT(*) FILTER (WHERE status = 'PRESENT') AS present,
                       COUNT(*) FILTER (WHERE status = 'ABSENT') AS absent,
                       COUNT(*) FILTER (WHERE status = 'LATE') AS late
                FROM staff_attendance_records
                WHERE school_id = ?
                  AND attendance_date BETWEEN ? AND ?
                GROUP BY attendance_date
                ORDER BY attendance_date
                """,
                (rs, rowNum) -> {
                    long recorded = rs.getLong("recorded");
                    long present = rs.getLong("present");
                    return new StaffAttendancePoint(
                            rs.getObject("attendance_date", LocalDate.class),
                            recorded,
                            present,
                            rs.getLong("absent"),
                            rs.getLong("late"),
                            percentage(present, recorded)
                    );
                },
                schoolId, start, end
        );
    }

    private FeeTotals feeTotals(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.queryForObject(
                """
                SELECT
                    COALESCE(SUM(amount), 0) AS invoiced,
                    COALESCE(SUM(paid_amount), 0) AS allocated_paid,
                    COALESCE(SUM(GREATEST(amount - paid_amount, 0)), 0) AS outstanding,
                    COUNT(*) FILTER (WHERE due_date < ? AND status <> 'PAID') AS overdue_count
                FROM fee_invoices
                WHERE school_id = ?
                  AND created_at::date BETWEEN ? AND ?
                """,
                (rs, rowNum) -> new FeeTotals(
                        decimal(rs.getBigDecimal("invoiced")),
                        decimal(rs.getBigDecimal("allocated_paid")),
                        decimal(rs.getBigDecimal("outstanding")),
                        rs.getLong("overdue_count"),
                        collectionRate(rs.getBigDecimal("allocated_paid"), rs.getBigDecimal("invoiced"))
                ),
                end, schoolId, start, end
        );
    }

    private List<CollectionPoint> collectionTrend(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.query(
                """
                SELECT
                    p.paid_at::date AS paid_date,
                    COALESCE(SUM(p.amount) FILTER (WHERE p.status = 'SUCCESS'), 0) AS collected,
                    COUNT(*) FILTER (WHERE p.status = 'SUCCESS') AS payment_count
                FROM fee_payments p
                JOIN fee_invoices i ON i.id = p.invoice_id
                WHERE i.school_id = ?
                  AND p.paid_at::date BETWEEN ? AND ?
                GROUP BY p.paid_at::date
                ORDER BY p.paid_at::date
                """,
                (rs, rowNum) -> new CollectionPoint(
                        rs.getObject("paid_date", LocalDate.class),
                        decimal(rs.getBigDecimal("collected")),
                        rs.getLong("payment_count")
                ),
                schoolId, start, end
        );
    }

    private AssessmentTotals assessmentTotals(UUID schoolId, LocalDate start, LocalDate end) {
        return jdbc.queryForObject(
                """
                SELECT
                    COUNT(DISTINCT a.id) AS assessments,
                    COUNT(ar.id) AS results,
                    COALESCE(SUM(ar.marks), 0) AS total_marks,
                    COALESCE(AVG(a.max_marks), 0) AS avg_max_marks
                FROM assessments a
                LEFT JOIN assessment_results ar
                    ON ar.assessment_id = a.id
                   AND ar.status = 'RECORDED'
                WHERE a.school_id = ?
                  AND a.assessment_date BETWEEN ? AND ?
                """,
                (rs, rowNum) -> new AssessmentTotals(
                        rs.getLong("assessments"),
                        rs.getLong("results"),
                        decimal(rs.getBigDecimal("total_marks")),
                        decimal(rs.getBigDecimal("avg_max_marks"))
                ),
                schoolId, start, end
        );
    }

    private static double percentage(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : (numerator * 100.0) / denominator;
    }

    private static BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static double collectionRate(BigDecimal collected, BigDecimal invoiced) {
        if (invoiced == null || invoiced.signum() == 0) {
            return 0.0;
        }
        return collected == null
                ? 0.0
                : collected.multiply(BigDecimal.valueOf(100))
                    .divide(invoiced, 2, RoundingMode.HALF_UP)
                    .doubleValue();
    }

    public record AnalyticsView(
            UUID schoolId,
            LocalDate from,
            LocalDate to,
            AttendanceTotals attendance,
            FeeTotals fees,
            AssessmentTotals assessments,
            List<AttendancePoint> attendanceTrend,
            List<StaffAttendancePoint> staffAttendanceTrend,
            List<CollectionPoint> collectionTrend
    ) {}

    public record AttendanceTotals(
            long recorded,
            long present,
            long absent,
            long late,
            long excused,
            double attendanceRate
    ) {}

    public record AttendancePoint(
            LocalDate date,
            long recorded,
            long present,
            long absent,
            long late,
            long excused,
            double attendanceRate
    ) {}

    public record StaffAttendancePoint(
            LocalDate date,
            long recorded,
            long present,
            long absent,
            long late,
            double attendanceRate
    ) {}

    public record FeeTotals(
            BigDecimal invoiced,
            BigDecimal allocatedPaid,
            BigDecimal outstanding,
            long overdueInvoiceCount,
            double collectionRate
    ) {}

    public record CollectionPoint(
            LocalDate date,
            BigDecimal collected,
            long paymentCount
    ) {}

    public record AssessmentTotals(
            long assessmentCount,
            long resultCount,
            BigDecimal totalMarksAwarded,
            BigDecimal averageAssessmentMaxMarks
    ) {}
}
