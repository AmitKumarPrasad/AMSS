create table staff_attendance_records (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    staff_id uuid not null references staff_members(id) on delete cascade,
    attendance_date date not null,
    status varchar(16) not null,
    recorded_by uuid not null references app_users(id),
    recorded_at timestamptz not null default now(),
    unique (staff_id, attendance_date)
);

create index idx_staff_attendance_school_staff_date
    on staff_attendance_records(school_id, staff_id, attendance_date desc);
