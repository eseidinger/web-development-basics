# Development Environment

Docker Compose stack for local development with all supporting services.

## Quick Start (Automated)

The easiest way to get everything running with Keycloak realm auto-configured:

```bash
cd environment
chmod +x setup.sh
./setup.sh
```

This will:
1. Start all Docker services
2. Wait for Keycloak to be ready
3. Automatically import the taskboard realm with clients and test users
4. Display access points and next steps

Then proceed to [Configure the Backend](#configure-the-backend).

---

## Manual Start

If you prefer to start services without auto-initialization:

```bash
docker compose up -d
```

| Service | Port | URL | Purpose |
|---|---|---|---|
| PostgreSQL | 5432 | localhost:5432 | Database for Task Board |
| Keycloak | 8090 | http://localhost:8090 | OIDC Provider / Authentication |
| Prometheus | 9090 | http://localhost:9090 | Metrics collection |
| Loki | 3100 | (internal) | Log aggregation |
| Jaeger | 16686 | http://localhost:16686 | Distributed tracing |
| Grafana | 3000 | http://localhost:3000 | Dashboards and visualization |
| Promtail | (internal) | (internal) | Log shipper to Loki |

## Getting Started

### 1. Start the stack

```bash
docker compose up -d
```

This starts all services. Use `docker compose logs -f` to watch logs.

### 2. Wait for services to be ready

All services have health checks. Check status with:

```bash
docker compose ps
```

### 3. Access the services

- **Keycloak Admin Console:** http://localhost:8090/admin  
  - Username: `admin`  
  - Password: `admin`

- **Grafana:** http://localhost:3000  
  - Username: `admin`  
  - Password: `admin`  
  - Datasources (Prometheus, Loki, Jaeger) are pre-configured

- **Prometheus:** http://localhost:9090

- **Jaeger UI:** http://localhost:16686

## Setting Up Keycloak for Task Board

### Automatic (Recommended)

If you used `./setup.sh`, the realm is already configured! Skip to [Configuring the Backend](#configuring-the-backend).

### Manual

If you want to set up the realm manually:

1. Log in to Keycloak at http://localhost:8090/admin  
   - Username: `admin`  
   - Password: `admin`

2. Import the pre-configured realm:
   - Go to **Realm settings** → **Action** menu → **Import**
   - Select `keycloak-realm-export.json`
   - Click **Import**

3. Verify clients and users were imported:
   - **Clients:** `taskboard-api`, `taskboard-frontend` should exist
   - **Users:** `alice`, `bob`, `charlie` with password `password`

### Pre-Configured Test Users

All users have the password `password`:

| Username | Email | Roles |
|---|---|---|
| alice | alice@taskboard.local | `user`, `admin` |
| bob | bob@taskboard.local | `user` |
| charlie | charlie@taskboard.local | `user` |

### Available Realm Roles

The taskboard realm includes these roles:

| Role | Description |
|---|---|
| `user` | Base user role - all authenticated users have this |
| `admin` | System administrator role (for future use) |

Board-specific roles (owner/member/viewer) are stored in the database via `board_membership.role`, not in Keycloak.

### Client Credentials

| Client | Secret | Purpose |
|---|---|---|
| `taskboard-api` | `taskboard-api-secret-dev-only-change-in-prod` | Backend API (server-to-server) |
| `taskboard-frontend` | (none, public client) | Frontend SPA |

## Configuring the Backend

In your Spring Boot `application-dev.yml`, configure OIDC:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8090/realms/taskboard
          jwk-set-uri: http://localhost:8090/realms/taskboard/protocol/openid-connect/certs

  datasource:
    url: jdbc:postgresql://localhost:5432/taskboard_dev
    username: taskboard
    password: taskboard_dev_password
```

Configure observability:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  pattern:
    console: "%d{ISO8601} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n"
```

Then run your backend:

```bash
cd ../taskboard-spring
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Your API will be available at `http://localhost:8080/api/v1`.

## Stopping the Stack

```bash
docker compose down
```

To also remove volumes (data):

```bash
docker compose down -v
```

## Architecture

```
┌──────────────────────────────────────┐
│      Backend Service                 │
│      (Spring Boot on :8080)          │
└────┬─────────────────────────────────┘
     │
     ├─── PostgreSQL (5432)            [Database]
     ├─── Keycloak (8090)              [OIDC Provider]
     ├─► Prometheus (9090)             [Metrics from /actuator/prometheus]
     └─► Jaeger (4318)                 [OpenTelemetry traces]
           │
           └─► Grafana (3000)          [Dashboards]
               ├─ Prometheus datasource
               ├─ Loki datasource
               └─ Jaeger datasource

Logs Flow:
Backend → Docker logs → Promtail → Loki → Grafana
```

## Tracing Integration

This setup uses **Jaeger** for distributed tracing.

To send traces from your Spring Boot app, add to `pom.xml`:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
  <groupId>io.opentelemetry.exporter</groupId>
  <artifactId>opentelemetry-exporter-jaeger</artifactId>
</dependency>
```

Configure in `application-dev.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # Sample 100% of traces in dev
  otlp:
    tracing:
      endpoint: http://localhost:4318
```

Then view traces at http://localhost:16686

## Prometheus Metrics

Prometheus is configured to scrape metrics from the backend at:

```
GET http://host.docker.internal:8080/actuator/prometheus
```

It scrapes every 10 seconds. Create custom dashboards in Grafana to visualize metrics.

## Loki Logs

Loki automatically collects logs from all Docker containers via Promtail. Query logs in Grafana using the Loki datasource.

Example query:

```
{job="taskboard-api"}
```

## Cleanup

To reset everything:

```bash
docker compose down -v
docker compose up -d
```

This removes all persisted data and starts fresh.

---

## Automation Files (How It Works)

If you're curious about the automation:

### `setup.sh`

Main setup script that orchestrates everything:
1. Checks Docker is running
2. Starts all services with `docker compose up -d`
3. Waits for Keycloak to be healthy
4. Runs `keycloak-init` service to import the realm
5. Displays access points and test credentials

Run with:
```bash
./setup.sh
```

### `keycloak-realm-export.json`

Pre-configured Keycloak realm export containing:
- **Realm:** `taskboard` (enabled)
- **Clients:** 
  - `taskboard-api` (confidential, for backend)
  - `taskboard-frontend` (public, for SPA)
- **Users:**
  - alice, bob, charlie (all with password: `password`)
- **Roles:** Default user roles

This file is imported automatically by the init script.

### `init-keycloak.sh`

Bash script that:
1. Waits for Keycloak to become healthy
2. Authenticates with the admin CLI
3. Checks if realm already exists (idempotent)
4. Imports the realm JSON via Keycloak admin REST API
5. Verifies successful import

The script runs in the `keycloak-init` service (defined in `docker-compose.yml`) with the `init` profile.

### `docker-compose.yml` - Keycloak Init Service

Added to compose file:
```yaml
keycloak-init:
  image: alpine:latest
  depends_on:
    keycloak:
      condition: service_healthy
  volumes:
    - ./keycloak-realm-export.json:/tmp/keycloak-realm-export.json:ro
    - ./init-keycloak.sh:/tmp/init-keycloak.sh:ro
  # ... installs curl & jq, then runs init script
  profiles:
    - init
```

The `profiles: [init]` means it only runs when explicitly requested (via `setup.sh`).

---

## Customizing the Realm

To modify the pre-configured realm:

1. Log into Keycloak admin console
2. Make changes (add clients, users, roles, etc.)
3. Export the realm: **Realm settings** → **Export**
4. Replace `keycloak-realm-export.json` with your export
5. Next time you run `setup.sh`, the updated realm will be imported

Or, edit `keycloak-realm-export.json` directly (it's just JSON).
