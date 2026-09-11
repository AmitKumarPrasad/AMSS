create table student_guardians (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    student_id uuid not null references students(id) on delete cascade,
    full_name varchar(200) not null,
    relationship varchar(64) not null,
    email varchar(254),
    phone varchar(32),
    is_primary boolean not null default false,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now()
);

create index idx_student_guardians_student_status on student_guardians(student_id, status, is_primary);
create index idx_student_guardians_school on student_guardians(school_id, status);
