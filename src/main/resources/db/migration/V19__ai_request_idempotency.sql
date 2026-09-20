CREATE TABLE IF NOT EXISTS ai_request_idempotency (
    school_id uuid NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    request_id uuid NOT NULL,
    status varchar(16) NOT NULL,
    answer text,
    evidence_json text,
    conversation_id uuid,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_ai_request_idempotency PRIMARY KEY (school_id, user_id, request_id),
    CONSTRAINT ck_ai_request_idempotency_status CHECK (status IN ('PROCESSING','COMPLETED'))
);

CREATE INDEX IF NOT EXISTS idx_ai_request_idempotency_updated
    ON ai_request_idempotency(updated_at DESC);
