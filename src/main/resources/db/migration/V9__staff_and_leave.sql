create table staff_members (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    employee_code varchar(64) not null,
    full_name varchar(200) not null,
    email varchar(254),
    phone varchar(32),
    designation varchar(120) not null,
    employment_type varchar(32) not null default 'FULL_TIME',
    joined_on date,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    unique (school_id, employee_code)
);

create table leave_requests (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    staff_id uuid not null references staff_members(id) on delete cascade,
    leave_type varchar(32) not null,
    starts_on date not null,
    ends_on date not null,
    reason varchar(1000),
    status varchar(32) not null default 'PENDING',
    reviewed_by uuid references app_users(id),
    reviewed_at timestamptz,
    created_at timestamptz not null default now()
);

create index idx_staff_school_status on staff_members(school_id, status, full_name);
create index idx_leave_school_status on leave_requests(school_id, status, starts_on);
create index idx_leave_staff_dates on leave_requests(staff_id, starts_on, ends_on);
