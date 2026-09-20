CREATE TABLE IF NOT EXISTS ai_conversations (
    id uuid PRIMARY KEY,
    school_id uuid NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    title varchar(200) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_ai_conversation_school_user_id UNIQUE (school_id, user_id, id)
);

CREATE INDEX IF NOT EXISTS idx_ai_conversations_school_user_updated
    ON ai_conversations(school_id, user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS ai_conversation_messages (
    id uuid PRIMARY KEY,
    conversation_id uuid NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    school_id uuid NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role varchar(16) NOT NULL,
    content text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_ai_conversation_message_role CHECK (role IN ('USER','ASSISTANT'))
);

CREATE INDEX IF NOT EXISTS idx_ai_conversation_messages_scope
    ON ai_conversation_messages(school_id, user_id, conversation_id, created_at DESC);
