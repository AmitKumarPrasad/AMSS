package com.edusphere.notifications;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationClaimRepository {
    private final JdbcTemplate jdbc;

    public NotificationClaimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<UUID> claimPending(Instant now, Instant staleBefore, String workerId) {
        return jdbc.query("""
                update notifications n
                set claimed_at = ?, claimed_by = ?
                where n.id in (
                    select id from notifications
                    where status = 'PENDING'
                      and available_at <= ?
                      and (claimed_at is null or claimed_at < ?)
                    order by available_at asc
                    for update skip locked
                    limit 100
                )
                returning id
                """, ps -> {
                    ps.setTimestamp(1, Timestamp.from(now));
                    ps.setString(2, workerId);
                    ps.setTimestamp(3, Timestamp.from(now));
                    ps.setTimestamp(4, Timestamp.from(staleBefore));
                }, (rs, rowNum) -> (UUID) rs.getObject("id"));
    }
}
