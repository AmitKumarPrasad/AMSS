create table assessments (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    academic_year_id uuid not null references academic_years(id),
    class_id uuid not null references classes(id),
    name varchar(120) not null,
    assessment_date date not null,
    max_marks numeric(8,2) not null,
    status varchar(32) not null default 'DRAFT',
    unique (class_id, name, assessment_date)
);

create table assessment_results (
    id uuid primary key default gen_random_uuid(),
    assessment_id uuid not null references assessments(id),
    student_id uuid not null references students(id),
    marks numeric(8,2) not null,
    grade varchar(16),
    status varchar(32) not null default 'RECORDED',
    recorded_by uuid references app_users(id),
    recorded_at timestamptz not null default now(),
    unique (assessment_id, student_id)
);

create index idx_assessments_school_date on assessments(school_id, assessment_date);
create index idx_assessment_results_student on assessment_results(student_id, assessment_id);
