alter table notifications
    add column read_at timestamptz;

create index idx_notifications_unread
    on notifications(school_id, recipient_user_id, read_at, created_at desc);
