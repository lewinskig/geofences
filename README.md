# Geofences - Proof of Concept

A demo application for server-side polygon geofence evaluation.
A tracker sends its location, and the server evaluates in real-time whether the tracker has entered/exited defined areas, publishing notifications via SSE.

## Tech Stack

- **Kotlin 2.2** + **Java 24**
- **Spring Boot 4.0** (WebMVC, JDBC, Actuator)
- **PostgreSQL** - persistence
- **Flyway** - database migrations
- **JTS (Java Topology Suite)** - geometric calculations (point-in-polygon)
- **Leaflet + leaflet-draw** - demo UI

## Requirements

- Java 24+
- PostgreSQL (e.g., [Postgres.app](https://postgresapp.com/)) running locally on default port 5432, no password

## Running

```bash
# 1. Create database
createdb geofences

# 2. Run application
./gradlew bootRun
```

Application starts at `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/tracker/{id}` | Last known tracker position |
| `POST` | `/location` | Update tracker position |
| `GET` | `/geofences` | List all geofences |
| `POST` | `/geofences` | Create new geofence |
| `DELETE` | `/geofences/{id}` | Delete geofence |
| `GET` | `/notifications` | SSE stream for transition notifications (ENTERED/EXITED) |
| `GET` | `/actuator/health` | Health check |

### Examples

**Send location:**
```bash
curl -X POST http://localhost:8080/location \
  -H "Content-Type: application/json" \
  -d '{"trackId": "tracker-1", "lat": 50.0617, "lng": 19.9373, "timestamp": "2026-03-22T12:00:00Z"}'
```

**Create geofence:**
```bash
curl -X POST http://localhost:8080/geofences \
  -H "Content-Type: application/json" \
  -d '{"name": "Market Square", "polygon": [{"lat": 50.062, "lng": 19.937}, {"lat": 50.061, "lng": 19.937}, {"lat": 50.061, "lng": 19.938}, {"lat": 50.062, "lng": 19.938}]}'
```

**Subscribe to notifications:**
```bash
curl http://localhost:8080/notifications
```

## Demo UI

The static file `frontend/index.html` contains a simple Leaflet map for testing:
- Displays tracker `tracker-1` position
- Allows drawing and deleting geofences
- Arrow keys (↑↓←→) move position by ~25m
- Geofence colors change on enter/exit (blue → green)

> **Note:** The UI is only an illustration for demo purposes, not production code.

## Architecture

This project intentionally chooses a **server-side geofence evaluation model** with a simple, explicit domain and strong consistency guarantees.

### Design Goals

The design prioritizes:
- **Deterministic behavior** – no lost transitions
- **Clarity of domain model** – easy to understand and reason about
- **Ease of testing** – pure domain logic separated from side-effects
- **Incremental extensibility** – structured for future evolution

### Evaluation Flow

1. Tracker sends location → `POST /location`
2. Server finds geofences whose bounding-box contains the point (fast pre-filter)
3. JTS checks point-in-polygon for candidates (precise evaluation)
4. Comparison with active geofences → ENTERED/EXITED detection
5. Notifications published via SSE

### Key Design Decisions

#### State as Transitions (not flags)

Instead of storing a boolean like `isInside`, the system models state as transitions (`ENTERED`, `EXITED`) and persists only:
- Active memberships (currently inside)
- Transition events

**Why:** Avoids inconsistent state, makes transitions explicit and auditable, aligns naturally with event-driven architectures.

#### Coarse + Exact Geometry Evaluation

Geofence evaluation is split into two phases:
1. **Bounding box (bbox)** → fast pre-filter
2. **JTS polygon check** → precise evaluation

**Why:** Reduces expensive geometry checks, enables early EXIT detection without JTS, keeps performance predictable without spatial DB.

#### Per-Tracker Transactional Processing

Each tracker is processed under `SELECT ... FOR UPDATE` within a single transaction.

**Why:** Guarantees no race conditions, ensures ordered processing per tracker, avoids complex distributed locking.

#### Tracker as State + Lock Anchor

The tracker table serves three purposes:
- 🔒 Lock anchor (row-level locking)
- 📌 Last known position
- ⏱ Stale event guard

**Why:** Eliminates need for separate "latest state" table, simplifies concurrency model, enables easy UI integration.

#### Separation of Domain and Side-Effects

The system clearly separates:
- Pure domain logic (`evaluateTransitions`)
- Side-effects (DB updates, notifications)

**Why:** Easier unit testing, better readability, easier to evolve (e.g. introduce outbox later).

#### Notification as Pluggable Adapter

Notifications are delivered through `NotificationService` (port) with multiple adapters (SSE, logging).

**Why:** Clean separation of concerns, easy to replace SSE with Kafka/SQS, demo-friendly while remaining extensible.

### Why Not PostGIS?

This project intentionally avoids pushing logic into the database.

**Why:** Keeps domain logic in application code, easier to test and debug, avoids tight coupling to a specific DB extension.

### Alternative Approaches

This architecture is one of several possibilities:

| Approach | Description | Trade-offs |
|----------|-------------|------------|
| **Client-side evaluation** | Mobile app checks geofences itself | Backend = simple CRUD; requires definition sync |
| **PostGIS evaluation** | Database executes ST_Contains | Less application code; logic in SQL |
| **Server-side evaluation** *(current)* | Application server computes geometry | Full control; easy testing; scalability needs consideration |

### Evolution Path

The design supports gradual evolution:
- Add **outbox pattern** → reliable delivery
- Replace **SSE with message broker** (Kafka, SQS)
- Introduce **PostGIS or spatial index**
- Add **multi-node support**

### Summary

> This design favors **clarity over cleverness**, **correctness over premature optimization**.
>
> It is intentionally simple, but structured in a way that can scale conceptually into a production-ready system.


## Simplifications and Limitations

This is a proof-of-concept, **not production code**. Intentionally omitted:

### Security
- No HTTPS / certificates
- No authorization / authentication
- PostgreSQL without password or authentication

### Database
- No dedicated schema (everything in `public`)
- Index only on time - should be on `trackerId + time`
- No testcontainers (tests require local Postgres)

### Application
- No global error handler
- Event publication and database write not in single transaction (no outbox pattern)
- No antimeridian handling (works correctly only in Europe)

## Tests

```bash
./gradlew test
```

> **Note:** Tests require PostgreSQL running on localhost:5432
