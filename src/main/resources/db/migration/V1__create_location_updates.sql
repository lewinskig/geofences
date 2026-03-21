create table location_updates
(
    id          bigserial primary key,
    track_id    varchar(255)     not null,
    latitude    double precision not null,
    longitude   double precision not null,
    recorded_at timestamptz      not null,
    received_at timestamptz      not null default now()
);

create index idx_location_updates_track_id on location_updates (track_id);
create index idx_location_updates_recorded_at on location_updates (recorded_at);