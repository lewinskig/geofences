create table active_geofence
(
    track_id    varchar(255) not null,
    geofence_id uuid         not null,
    entered_at  timestamptz  not null,
    primary key (track_id, geofence_id),
    foreign key (geofence_id) references geofence_definition (id)
);

create index idx_active_geofence_track_id
    on active_geofence (track_id);