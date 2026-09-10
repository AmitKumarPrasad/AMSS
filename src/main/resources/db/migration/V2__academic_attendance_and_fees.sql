create table academic_years (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    code varchar(32) not null,
    name varchar(120) not null,
    starts_on date not null,
    ends_on date not null,
    status varchar(32) not null default 'PLANNED',
    unique (school_id, code)
);

create table classes (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    academic_year_id uuid not null references academic_years(id),
    name varchar(80) not null,
    grade_level integer not null,
    unique (academic_year_id, name)
);

create table sections (
    id uuid primary key default gen_random_uuid(),
    class_id uuid not null references classes(id),
    name varchar(40) not null,
    room varchar(40),
    unique (class_id, name)
);

create table enrollments (
    id uuid primary key default gen_random_uuid(),
    student_id uuid not null references students(id),
    section_id uuid not null references sections(id),
    enrolled_on date not null,
    status varchar(32) not null default 'ACTIVE',
    unique (student_id, section_id)
);

create table attendance_records (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    student_id uuid not null references students(id),
    attendance_date date not null,
    status varchar(32) not null,
    source varchar(32) not null default 'MANUAL',
    recorded_by uuid references app_users(id),
    unique (student_id, attendance_date)
);

create table fee_invoices (
    id uuid primary key default gen_random_uuid(),
    school_id uuid not null references schools(id),
    student_id uuid not null references students(id),
    invoice_number varchar(64) not null,
    due_date date not null,
    amount numeric(14,2) not null,
    paid_amount numeric(14,2) not null default 0,
    status varchar(32) not null default 'OPEN',
    created_at timestamptz not null default now(),
    unique (school_id, invoice_number)
);

create table fee_payments (
    id uuid primary key default gen_random_uuid(),
    invoice_id uuid not null references fee_invoices(id),
    payment_reference varchar(100) not null unique,
    amount numeric(14,2) not null,
    paid_at timestamptz not null default now(),
    status varchar(32) not null default 'SUCCESS'
);

create index idx_attendance_student_date on attendance_records(student_id, attendance_date desc);
create index idx_invoice_student_status on fee_invoices(student_id, status);
