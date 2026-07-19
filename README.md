# Task Board: Web Development Reference Application

A reference application demonstrating core web development patterns across the full stack: business requirements, system architecture, database design, API contracts, authentication and authorization, and observability.

## Project Overview

Task Board is a collaborative work management tool that helps teams organize, track, and progress tasks through defined workflow stages. It serves as a learning resource for:

- Understanding how business requirements translate to technical design
- Designing and implementing REST APIs with role-based access control
- Building relational database schemas with proper constraints and indexes
- Implementing authentication and authorization in web applications
- Designing systems with observability from the start
- Establishing clear boundaries between frontend, backend, database, and external services

## Documentation

All application documentation is in the `documentation/` folder:

- **[task-board.md](documentation/task-board.md)** — Business requirements, user roles, features, workflows, and business rules.
- **[architecture.md](documentation/architecture.md)** — System components, their responsibilities, communication patterns, and design principles.
- **[api.md](documentation/api.md)** — REST API contracts: endpoints, request/response formats, error handling, and authorization rules.
- **[db-schema.md](documentation/db-schema.md)** — Relational schema design, entities, relationships, constraints, and indexes.
- **[spring.md](documentation/spring.md)** — Spring Boot implementation reference (optional; shows one way to implement the design).

## Project Structure

```
web-development-basics/
├── README.md                          # This file
├── context/                           # Reference articles on frameworks and practices
│   ├── page-frontend-web-development-frameworks.md
│   ├── page-java-web-development-frameworks.md
│   └── page-what-every-web-developer-should-know-and-where-ai-can-help.md
├── documentation/                     # Application documentation
│   ├── task-board.md                  # Business requirements
│   ├── architecture.md                # System design
│   ├── api.md                         # REST API reference
│   ├── db-schema.md                   # Database schema
│   └── spring.md                      # Spring Boot implementation guide
└── taskboard-spring/                  # Spring Boot backend implementation
    ├── pom.xml
    ├── src/
    └── ...
```

## How to Use This Project

1. **Understand the business domain**

   Read [task-board.md](documentation/task-board.md) to understand what the application does, who uses it, and what features it provides.

2. **Review the system design**

   Read [architecture.md](documentation/architecture.md) to understand how the system is structured: what components exist, what each is responsible for, and how they communicate.

3. **Study the data model**

   Read [db-schema.md](documentation/db-schema.md) to understand how data is organized, what relationships exist, and what constraints enforce data integrity.

4. **Review the API contracts**

   Read [api.md](documentation/api.md) to understand what operations are available, how to call them, what they return, and what errors can occur.

5. **Implement**

   Use the framework and language of your choice. [spring.md](documentation/spring.md) provides one example implementation in Spring Boot; the design is framework-agnostic and can be implemented in any technology stack.

## System Architecture

The application is organized into four independent layers that communicate through well-defined interfaces:

```
┌──────────────────────┐
│  Frontend            │  (Browser-based SPA)
│  (React/Vue/Angular) │
└─────────┬────────────┘
          │ HTTPS REST + Bearer Token
          │
    ┌─────v──────────────┐
    │  Backend           │  (Stateless REST API Service)
    │  (Spring/Quarkus/  │
    │   Other Framework) │
    └─────┬──────────────┘
          │ SQL
    ┌─────v──────────────┐
    │  Database          │  (PostgreSQL / Other RDBMS)
    │  (Relational)      │
    └────────────────────┘

    │ + External Services:
    ├─ OIDC Provider (Authentication/Token Issuance)
    └─ Observability Platform (Logs/Metrics/Traces)
```

All components emit telemetry (logs, metrics, traces) to a centralized observability platform for production visibility.

## Key Design Principles

- **Stateless backend** — all identity context is carried in the access token; no session state is stored
- **Role-based authorization** — access control is enforced independently at the backend; frontend rules are for UX only
- **Separation of concerns** — frontend, backend, database, and identity provider are independent layers
- **Externalized configuration** — environment-specific settings (database URLs, secrets, OIDC endpoints) are supplied at runtime, not baked into code
- **Observability from the start** — all components emit structured logs, metrics, and traces to support production debugging
- **Schema migration discipline** — database changes are versioned and applied in order; no ad hoc modifications
