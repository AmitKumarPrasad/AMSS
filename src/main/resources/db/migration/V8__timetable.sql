create table timetable_entries (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    section_id uuid not null references sections(id) on delete cascade,
    subject_id uuid not null references subjects(id),
    day_of_week varchar(16) not null,
    starts_at time not null,
    ends_at time not null,
    room varchar(40),
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    unique (section_id, day_of_week, starts_at)
);

create index idx_timetable_section_day on timetable_entries(section_id, day_of_week, starts_at);
