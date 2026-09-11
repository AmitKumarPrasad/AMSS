create table notifications (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    recipient_user_id uuid not null references app_users(id),
    channel varchar(16) not null,
    subject varchar(200),
    body text not null,
    status varchar(24) not null default 'PENDING',
    attempts integer not null default 0,
    available_at timestamptz not null default now(),
    last_error text,
    sent_at timestamptz,
    created_at timestamptz not null default now()
);
create index idx_notifications_delivery on notifications(status, available_at);
create index idx_notifications_recipient on notifications(school_id, recipient_user_id, created_at desc);
