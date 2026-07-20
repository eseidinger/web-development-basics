# Development Environment

Docker Compose stack for local development with supporting services.

## Quick Start

```bash
cd environment
chmod +x setup.sh
./setup.sh
```

This will:

1. Start all local Docker services
2. Wait briefly for services to initialize
3. Print service access points and OIDC settings

Then proceed to Configure the Backend.

## Included Local Services

| Service | Port | URL | Purpose |
| --- | --- | --- | --- |
| PostgreSQL | 5432 | `localhost:5432` | Database for Task Board |
| Prometheus | 9090 | `http://localhost:9090` | Metrics collection |
| Loki | 3100 | `(internal)` | Log aggregation |
| Jaeger | 16686 | `http://localhost:16686` | Distributed tracing |
| Grafana | 3000 | `http://localhost:3000` | Dashboards and visualization |
| Promtail | (internal) | `(internal)` | Log shipper to Loki |

## OIDC Provider

Authentication is provided by a hosted Keycloak realm:

- Discovery: `https://keycloak.eseidinger.de/realms/taskboard-dev/.well-known/openid-configuration`
- Issuer: `https://keycloak.eseidinger.de/realms/taskboard-dev`
- Client ID: taskboard

No local Keycloak container or realm import is required.

## Configuring the Backend

In your Spring Boot `application-dev.yml` (or `.env`), configure OIDC:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://keycloak.eseidinger.de/realms/taskboard-dev
          jwk-set-uri: https://keycloak.eseidinger.de/realms/taskboard-dev/protocol/openid-connect/certs

  datasource:
    url: jdbc:postgresql://localhost:5432/taskboard_dev
    username: taskboard
    password: taskboard_dev_password
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
