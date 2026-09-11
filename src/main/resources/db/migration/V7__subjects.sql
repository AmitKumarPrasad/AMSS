create table subjects (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    code varchar(32) not null,
    name varchar(120) not null,
    status varchar(32) not null default 'ACTIVE',
    unique (school_id, code)
);

create table class_subjects (
    id uuid primary key default gen_random_uuid(),
    class_id uuid not null references classes(id) on delete cascade,
    subject_id uuid not null references subjects(id),
    status varchar(32) not null default 'ACTIVE',
    unique (class_id, subject_id)
);

create index idx_subjects_school_status on subjects(school_id, status, name);
create index idx_class_subjects_class on class_subjects(class_id, status);
