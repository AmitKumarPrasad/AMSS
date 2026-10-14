create table production_school_setups (
    source_user_id uuid primary key references app_users(id),
    school_id uuid not null unique references schools(id),
    admin_user_id uuid not null unique references app_users(id),
    created_at timestamptz not null default now()
);
