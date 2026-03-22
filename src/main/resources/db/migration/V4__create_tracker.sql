create table tracker
(
    id                  varchar(255) primary key,
    last_seen_latitude  double precision,
    last_seen_longitude double precision,
    last_recorded_at         timestamptz,
    last_received_at         timestamptz
);