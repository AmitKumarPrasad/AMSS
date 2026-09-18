alter table classes add column status varchar(32) not null default 'ACTIVE';
alter table sections add column status varchar(32) not null default 'ACTIVE';
create index idx_classes_school_year_status on classes(school_id, academic_year_id, status);
create index idx_sections_class_status on sections(class_id, status);
