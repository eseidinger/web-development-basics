# Task Board Spring Backend

Spring Boot backend for the Task Board application.

## Prerequisites

- Java 25 installed locally
- Maven available, or use the included Maven wrapper
- PostgreSQL and Keycloak running from the local environment stack

The backend reads local development settings from [.env](/home/eseidinger/dev/web-development-basics/taskboard-spring/.env).

## Start the local services

From the workspace root, start the shared development environment first:

```bash
cd /home/eseidinger/dev/web-development-basics/environment
./setup.sh
```

That starts:

- PostgreSQL on `localhost:5432`
- Keycloak on `http://localhost:8090`
- the `taskboard` realm with the frontend and API clients

## Run the Spring application

From this module directory:

```bash
cd /home/eseidinger/dev/web-development-basics/taskboard-spring
./mvnw spring-boot:run
```

You can also use a system Maven installation:

```bash
cd /home/eseidinger/dev/web-development-basics/taskboard-spring
mvn spring-boot:run
```

Use `spring-boot:run`, not `boot:run`.

## Why this works

The application imports local environment values from [.env](/home/eseidinger/dev/web-development-basics/taskboard-spring/.env) through [src/main/resources/application.properties](/home/eseidinger/dev/web-development-basics/taskboard-spring/src/main/resources/application.properties).

Those values provide:

- PostgreSQL connection settings
- Keycloak issuer URI
- Keycloak JWKS URI for JWT validation

No separate Spring profile is required for local development.

## Default local endpoints

- API base URL: `http://localhost:8080/api/v1`
- Health endpoint: `http://localhost:8080/actuator/health`
- Prometheus endpoint: `http://localhost:8080/actuator/prometheus`

## Test the backend

Run the test suite with:

```bash
cd /home/eseidinger/dev/web-development-basics/taskboard-spring
./mvnw test
```

## Local authentication

The backend is configured as an OAuth2 resource server and validates Keycloak access tokens.

Local Keycloak test users:

- `alice` / `password`
- `bob` / `password`
- `charlie` / `password`

Board permissions are enforced from the application database via `board_membership.role`, not from Keycloak realm roles.

## Troubleshooting

If startup fails:

1. Check that PostgreSQL is reachable on `localhost:5432`.
2. Check that Keycloak is reachable at `http://localhost:8090/realms/taskboard`.
3. Verify the realm exists by running the environment setup again.
4. Make sure you are using `spring-boot:run` instead of `boot:run`.