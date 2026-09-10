create table documents (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    title varchar(200) not null,
    description varchar(500),
    file_name varchar(255) not null,
    content_type varchar(120) not null,
    storage_key varchar(500) not null,
    checksum varchar(128),
    audience_role varchar(32),
    status varchar(32) not null default 'DRAFT',
    uploaded_by uuid not null references app_users(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (school_id, storage_key)
);

create index idx_documents_school_status on documents(school_id, status, created_at desc);
create index idx_documents_school_audience on documents(school_id, audience_role, status);
