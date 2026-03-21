create table geofence_definition
(
    id               uuid primary key,
    name             varchar(255)     not null,
    geometryWkt      text             not null,
    envelope_min_lat double precision not null,
    envelope_min_lng double precision not null,
    envelope_max_lat double precision not null,
    envelope_max_lng double precision not null,
    created_at       timestamptz      not null default now()
);