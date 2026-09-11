alter table notifications
    add column claimed_at timestamptz,
    add column claimed_by varchar(128);

create index idx_notifications_claimable
    on notifications(status, available_at, claimed_at);
