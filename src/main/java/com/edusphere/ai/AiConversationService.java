package com.edusphere.ai;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AiConversationService {
    public static final int MAX_TITLE = 200;
    public static final int MAX_MESSAGE = 2000;
    public static final int MAX_HISTORY = 12;
    private static final int MAX_CONVERSATIONS = 50;
    private static final int MAX_MESSAGES = 100;

    private final JdbcTemplate jdbc;

    public AiConversationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public ConversationView create(UUID schoolId, UUID userId, String title) {
        String safeTitle = title == null || title.isBlank() ? "New conversation" : title.trim();
        if (safeTitle.length() > MAX_TITLE) {
            throw new ResponseStatusException(BAD_REQUEST, "title must not exceed 200 characters");
        }
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO ai_conversations(id, school_id, user_id, title)
                VALUES (?, ?, ?, ?)
                """, id, schoolId, userId, safeTitle);
        return get(schoolId, userId, id);
    }

    public List<ConversationSummary> list(UUID schoolId, UUID userId, int limit) {
        if (limit < 1 || limit > MAX_CONVERSATIONS) {
            throw new ResponseStatusException(BAD_REQUEST, "limit must be between 1 and 50");
        }
        return jdbc.query("""
                SELECT id, title, created_at, updated_at
                FROM ai_conversations
                WHERE school_id = ? AND user_id = ?
                ORDER BY updated_at DESC
                LIMIT ?
                """, (rs, row) -> new ConversationSummary(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)), schoolId, userId, limit);
    }

    public ConversationView get(UUID schoolId, UUID userId, UUID conversationId) {
        List<ConversationView> conversations = jdbc.query("""
                SELECT id, title, created_at, updated_at
                FROM ai_conversations
                WHERE id = ? AND school_id = ? AND user_id = ?
                """, (rs, row) -> new ConversationView(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class),
                        List.of()), conversationId, schoolId, userId);
        if (conversations.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "conversation not found");
        }
        ConversationView base = conversations.get(0);
        List<MessageView> messages = jdbc.query("""
                SELECT id, role, content, created_at
                FROM ai_conversation_messages
                WHERE conversation_id = ? AND school_id = ? AND user_id = ?
                ORDER BY created_at ASC
                """, (rs, row) -> new MessageView(
                        rs.getObject("id", UUID.class),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getObject("created_at", OffsetDateTime.class)), conversationId, schoolId, userId);
        return new ConversationView(base.id(), base.title(), base.createdAt(), base.updatedAt(), messages);
    }

    public List<MessageView> messages(UUID schoolId, UUID userId, UUID conversationId, int limit) {
        if (limit < 1 || limit > MAX_MESSAGES) {
            throw new ResponseStatusException(BAD_REQUEST, "limit must be between 1 and 100");
        }
        ensureOwned(schoolId, userId, conversationId);
        return jdbc.query("""
                SELECT id, role, content, created_at
                FROM (
                    SELECT id, role, content, created_at
                    FROM ai_conversation_messages
                    WHERE conversation_id = ? AND school_id = ? AND user_id = ?
                    ORDER BY created_at DESC
                    LIMIT ?
                ) recent
                ORDER BY created_at ASC
                """, (rs, row) -> new MessageView(
                        rs.getObject("id", UUID.class),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getObject("created_at", OffsetDateTime.class)), conversationId, schoolId, userId, limit);
    }

    @Transactional
    public ConversationView rename(UUID schoolId, UUID userId, UUID conversationId, String title) {
        String safeTitle = title == null || title.isBlank() ? "New conversation" : title.trim();
        if (safeTitle.length() > MAX_TITLE) {
            throw new ResponseStatusException(BAD_REQUEST, "title must not exceed 200 characters");
        }
        ensureOwned(schoolId, userId, conversationId);
        jdbc.update("""
                UPDATE ai_conversations SET title = ?, updated_at = now()
                WHERE id = ? AND school_id = ? AND user_id = ?
                """, safeTitle, conversationId, schoolId, userId);
        return get(schoolId, userId, conversationId);
    }

    public List<HistoryMessage> history(UUID schoolId, UUID userId, UUID conversationId) {
        get(schoolId, userId, conversationId);
        return jdbc.query("""
                SELECT role, content
                FROM (
                    SELECT role, content, created_at
                    FROM ai_conversation_messages
                    WHERE conversation_id = ? AND school_id = ? AND user_id = ?
                    ORDER BY created_at DESC
                    LIMIT ?
                ) recent
                ORDER BY created_at ASC
                """, (rs, row) -> new HistoryMessage(rs.getString("role"), rs.getString("content")),
                conversationId, schoolId, userId, MAX_HISTORY);
    }

    @Transactional
    public void append(UUID schoolId, UUID userId, UUID conversationId, String role, String content) {
        if (!"USER".equals(role) && !"ASSISTANT".equals(role)) {
            throw new ResponseStatusException(BAD_REQUEST, "unsupported message role");
        }
        if (content == null || content.isBlank() || content.length() > MAX_MESSAGE) {
            throw new ResponseStatusException(BAD_REQUEST, "message must be 1 to 2000 characters");
        }
        ensureOwned(schoolId, userId, conversationId);
        jdbc.update("""
                INSERT INTO ai_conversation_messages(id, conversation_id, school_id, user_id, role, content)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), conversationId, schoolId, userId, role, content);
        jdbc.update("""
                UPDATE ai_conversations SET updated_at = now()
                WHERE id = ? AND school_id = ? AND user_id = ?
                """, conversationId, schoolId, userId);
    }

    @Transactional
    public void delete(UUID schoolId, UUID userId, UUID conversationId) {
        int deleted = jdbc.update("""
                DELETE FROM ai_conversations
                WHERE id = ? AND school_id = ? AND user_id = ?
                """, conversationId, schoolId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(NOT_FOUND, "conversation not found");
        }
    }

    private void ensureOwned(UUID schoolId, UUID userId, UUID conversationId) {
        Integer count = jdbc.queryForObject("""
                SELECT count(*) FROM ai_conversations
                WHERE id = ? AND school_id = ? AND user_id = ?
                """, Integer.class, conversationId, schoolId, userId);
        if (count == null || count != 1) {
            throw new ResponseStatusException(NOT_FOUND, "conversation not found");
        }
    }

    public record ConversationSummary(UUID id, String title, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
    public record ConversationView(UUID id, String title, OffsetDateTime createdAt,
                                   OffsetDateTime updatedAt, List<MessageView> messages) {}
    public record MessageView(UUID id, String role, String content, OffsetDateTime createdAt) {}
    public record HistoryMessage(String role, String content) {}
}
