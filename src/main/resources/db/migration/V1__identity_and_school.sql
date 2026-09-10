create extension if not exists pgcrypto;
create extension if not exists vector;

create table schools (
    id uuid primary key default gen_random_uuid(),
    tenant_code varchar(64) not null unique,
    name varchar(200) not null,
    timezone varchar(64) not null default 'Asia/Kolkata',
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table app_users (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    username varchar(120) not null,
    email varchar(254) not null,
    password_hash varchar(255) not null,
    display_name varchar(200) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (school_id, username),
    unique (school_id, email)
);

create table roles (
    id uuid primary key default gen_random_uuid(),
    code varchar(64) not null unique,
    name varchar(120) not null
);

create table user_roles (
    user_id uuid not null references app_users(id) on delete cascade,
    role_id uuid not null references roles(id) on delete cascade,
    primary key (user_id, role_id)
);

create table students (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    admission_number varchar(64) not null,
    first_name varchar(120) not null,
    last_name varchar(120),
    date_of_birth date,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (school_id, admission_number)
);

create table audit_events (
    id uuid primary key default gen_random_uuid(),
    school_id uuid references schools(id),
    actor_user_id uuid references app_users(id),
    event_type varchar(120) not null,
    aggregate_type varchar(120),
    aggregate_id uuid,
    payload jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create index idx_students_school_status on students(school_id, status);
create index idx_audit_school_created on audit_events(school_id, created_at desc);
