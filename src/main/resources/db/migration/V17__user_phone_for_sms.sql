alter table app_users add column phone varchar(32);
create index idx_app_users_school_phone on app_users(school_id, phone);
