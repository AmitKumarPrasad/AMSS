create table announcements (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    title varchar(160) not null,
    body text not null,
    audience_role varchar(32),
    published_at timestamptz,
    status varchar(32) not null default 'DRAFT',
    created_by uuid not null references app_users(id),
    created_at timestamptz not null default now()
);

create table announcement_reads (
    announcement_id uuid not null references announcements(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    read_at timestamptz not null default now(),
    primary key (announcement_id, user_id)
);

create index idx_announcements_school_status on announcements(school_id, status, published_at desc);
create index idx_announcement_reads_user on announcement_reads(user_id, read_at desc);
