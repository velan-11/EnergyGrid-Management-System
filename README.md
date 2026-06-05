# ⚡ EnergyGrid Management System

A full-stack, microservices-based platform for managing a modern electrical grid — from generation scheduling and asset telemetry to outage response, field work orders, demand-response programs, and customer billing. Built with **Spring Boot 3.5 / Java 21** on the backend and **Angular 21** on the frontend, wired together with **Spring Cloud** (Eureka, Config Server, API Gateway).

> The system models the real-world operation of a utility grid. Operators schedule and dispatch generation, technicians service grid assets, producers feed energy in, auditors review every action, and customers receive and pay invoices — all coordinated across independent, individually deployable services.

---

## 📑 Table of Contents

1. [Architecture Overview](#-architecture-overview)
2. [The Services](#-the-services)
3. [Roles & Access](#-roles--access)
4. [Tech Stack](#-tech-stack)
5. [Prerequisites](#-prerequisites)
6. [Databases You Must Create](#-databases-you-must-create)
7. [Configuration (Config Server)](#-configuration-config-server)
8. [Running the Project](#-running-the-project)
9. [Service Ports Reference](#-service-ports-reference)
10. [API Reference](#-api-reference)
11. [Cross-Cutting Features](#-cross-cutting-features)
12. [Project Layout](#-project-layout)
13. [Troubleshooting](#-troubleshooting)

---

## 🏛 Architecture Overview

EnergyGrid follows a classic **Spring Cloud microservices** pattern. Every business capability is its own service with its own database (database-per-service), and clients never talk to services directly — they go through a single **API Gateway**.

```
                         ┌───────────────────────────┐
                         │     Angular 21 Frontend     │
                         │        (localhost:4200)     │
                         └─────────────┬───────────────┘
                                       │  HTTP / JWT
                                       ▼
                         ┌───────────────────────────┐
                         │      API Gateway (9090)     │
                         │   Spring Cloud Gateway      │
                         │   routing • CORS • JWT       │
                         └─────────────┬───────────────┘
            ┌──────────────┬───────────┼───────────┬───────────────┐
            ▼              ▼           ▼            ▼               ▼
      ┌──────────┐  ┌──────────┐ ┌──────────┐ ┌──────────┐  ┌──────────────┐
      │ Identity │  │  Asset   │ │Scheduling│ │  Outage  │  │   Billing    │  ...
      │  (8081)  │  │  (8082)  │ │  (8083)  │ │  (8085)  │  │   (8086)     │
      └────┬─────┘  └────┬─────┘ └────┬─────┘ └────┬─────┘  └──────┬───────┘
           │             │            │            │               │
        identity_db   asset_db   scheduling_db  outage_db      billing_db   (MySQL, one DB per service)

      ┌─────────────────────────┐        ┌─────────────────────────┐
      │   Eureka Server (8761)  │◄───────►│  Config Server (8888)   │
      │   service discovery      │        │  centralised config      │
      └─────────────────────────┘        └────────────┬────────────┘
                                                       │ pulls from
                                          ┌────────────▼────────────┐
                                          │  Remote Git config repo  │
                                          └──────────────────────────┘
```

**How a request flows:**
1. The Angular app authenticates against `/api/auth/login` and receives a **JWT**.
2. Every subsequent call carries the JWT and hits the **API Gateway** (`:9090`).
3. The Gateway matches the path (e.g. `/api/assets/**`) and forwards to the right service.
4. Each service validates the JWT, applies role-based access, performs its work against **its own MySQL database**, writes an **audit log**, and (where relevant) emits a **notification**.
5. Services register themselves with **Eureka** for discovery and pull their configuration from the **Config Server**.

---

## 🧩 The Services

There are **11 modules** under `energygrid-microservices/`. Three are infrastructure, eight are business services.

### Infrastructure

| Service | Port | What it does |
|---|---|---|
| **eureka-server** | 8761 | **Service discovery.** Every other service registers here so they can find each other by name instead of hardcoded hosts. Start this first. |
| **config-server** | 8888 | **Centralised configuration.** Serves each service's settings (ports, datasource URLs, JWT secrets) by reading a remote Git repository. Start this second. |
| **api-gateway** | 9090 | **Single entry point.** Routes external requests to the correct service, applies global CORS, allows large multipart uploads (≈12 MB for evidence files), and is the only port the frontend needs to know about. |

### Business Services

| Service | Port | Database | Responsibility |
|---|---|---|---|
| **identity-service** | 8081 | `identity_db` | Authentication & user management. Registration, login, JWT issuance, password/username recovery (email), and admin approval/soft-deletion of users. |
| **asset-service** | 8082 | `asset_db` | Grid asset registry & telemetry. CRUD for physical assets (transformers, lines, meters, generators) plus ingestion of telemetry data points. |
| **scheduling-service** | 8083 | `scheduling_db` | Generation scheduling & dispatch. Operators plan how much energy each asset generates and when, then issue dispatch records that move a schedule into execution. |
| **demand-response-service** | 8084 | `demand_response_db` | Demand-response programs & events. Defines programs, fires DR events to shed/shift load, and tracks participant enrolment, performance reporting, and verification. |
| **outage-service** | 8085 | `outage_db` | Outage & incident management. Logs grid outages, tracks their lifecycle, and spawns incident tasks that field crews act on (with evidence attached). |
| **billing-service** | 8086 | `billing_db` | Billing, invoicing & payments. Generates invoices (with line items) for customers, tracks invoice status, and records payments. |
| **workorder-service** | 8087 | `workorder_db` | Field work orders. Creates and assigns maintenance work orders to technicians, tracks status, and stores uploaded maintenance evidence (photos/documents). |
| **notification-service** | 8088 | `notification_db` | Notifications. Persists and serves in-app notifications to users; supports mark-as-read and bulk read. |

> **Audit everywhere:** every business service also exposes an audit endpoint under `/api/audit/<service>` (and identity under `/api/identity/audit`). The admin "Audit" page in the frontend fans out to all of them in parallel and merges the results into one timeline.

---

## 👤 Roles & Access

Users have one of six roles (stored on the `users` table):

| Role | Typical actions |
|---|---|
| **ADMIN** | Approve/reject pending registrations, soft-delete & restore users, view the federated audit log. |
| **OPERATOR** | Create generation schedules, dispatch generation, declare outages, fire demand-response events. |
| **TECHNICIAN** | Receive and action assigned work orders & incident tasks, upload maintenance evidence. |
| **PRODUCER** | Register/operate generation assets, participate in demand-response programs. |
| **CUSTOMER** | View their invoices and pay them, receive notifications. |
| **AUDITOR** | Read-only access to audit logs across services. |

**Account lifecycle:** `CUSTOMER` accounts are `ACTIVE` immediately on registration. All other roles register as `PENDING` and must be approved by an `ADMIN` before they can log in. Users can be `ACTIVE`, `PENDING`, `INACTIVE`, or `LOCKED`, and are soft-deleted (never hard-deleted) for audit integrity.

---

## 🛠 Tech Stack

**Backend**
- Java 21, Spring Boot 3.5.x
- Spring Cloud — Netflix Eureka (discovery), Config Server, Spring Cloud Gateway
- Spring Data JPA / Hibernate (`ddl-auto=update`)
- Spring Security + JWT
- Spring Boot Mail (identity-service only, for credential recovery)
- MySQL 8
- Maven (with the Maven wrapper `mvnw.cmd`)

**Frontend**
- Angular 21 (standalone components)
- RxJS 7
- `jwt-decode` for client-side token handling
- Vitest + jsdom for unit tests
- Prettier

---

## ✅ Prerequisites

Install these before you start:

| Tool | Version | Notes |
|---|---|---|
| **JDK** | 21 | Required by all backend services. |
| **MySQL** | 8.x | Default config expects `localhost:3306`, user `root`, password `root`. The Windows service is typically `MySQL80`. |
| **Node.js** | 20+ | For the Angular frontend. |
| **npm** | 11.x | Pinned via `packageManager` in `package.json`. |
| **Git** | any | Config Server pulls configuration from a remote Git repo. |
| Maven | — | Not required globally; each service ships the `mvnw.cmd` wrapper. |

> `mysql.exe` is **not** on the PATH by default on the dev machine. It lives at
> `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`.

---

## 🗄 Databases You Must Create

The system uses **database-per-service**. Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) the first time each service starts — **but the databases themselves must already exist.** Create all eight before running anything:

```sql
CREATE DATABASE IF NOT EXISTS identity_db;
CREATE DATABASE IF NOT EXISTS asset_db;
CREATE DATABASE IF NOT EXISTS scheduling_db;
CREATE DATABASE IF NOT EXISTS demand_response_db;
CREATE DATABASE IF NOT EXISTS outage_db;
CREATE DATABASE IF NOT EXISTS billing_db;
CREATE DATABASE IF NOT EXISTS workorder_db;
CREATE DATABASE IF NOT EXISTS notification_db;
```

Run it on Windows like so:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e `
  "CREATE DATABASE IF NOT EXISTS identity_db; CREATE DATABASE IF NOT EXISTS asset_db; CREATE DATABASE IF NOT EXISTS scheduling_db; CREATE DATABASE IF NOT EXISTS demand_response_db; CREATE DATABASE IF NOT EXISTS outage_db; CREATE DATABASE IF NOT EXISTS billing_db; CREATE DATABASE IF NOT EXISTS workorder_db; CREATE DATABASE IF NOT EXISTS notification_db;"
```

| Database | Owned by | Key tables (auto-created) |
|---|---|---|
| `identity_db` | identity-service | `users`, `password_reset_token`, `audit_log` |
| `asset_db` | asset-service | `asset`, `telemetry_point`, `audit_log` |
| `scheduling_db` | scheduling-service | `generation_schedule`, `dispatch_record`, `audit_log` |
| `demand_response_db` | demand-response-service | `demand_response_program`, `demand_response_event`, `demand_response_participation`, `audit_log` |
| `outage_db` | outage-service | `outage`, `incident_task`, `audit_log` |
| `billing_db` | billing-service | `billing`, `invoice`, `invoice_line_item`, `payment`, `audit_log` |
| `workorder_db` | workorder-service | `work_order`, `technician`, `maintenance_evidence`, `audit_log` |
| `notification_db` | notification-service | `notification`, `incident_task`, `audit_log` |

> If your MySQL credentials differ from `root` / `root`, update them in the **Config Server's remote repo** (see below), not in the local service files.

---

## ⚙️ Configuration (Config Server)

Business services keep almost no config locally. Their local `application.properties` only sets the application name and tells them to import config from the Config Server:

```properties
spring.application.name=<service-name>
spring.config.import=optional:configserver:http://localhost:8888
```

The **Config Server** in turn reads its values from a **remote Git repository** — one `<service>.properties` file per service — at:

```
https://github.com/velan-2484878/energy-grid-config-server.git
```

That remote repo holds the real port, datasource URL/username/password, JWT secret, and mail settings for each service. **To change a database name, password, or port, edit the file in that config repo** (e.g. `asset-service.properties`) and restart the affected service.

---

## ▶️ Running the Project

### 1. Start MySQL
Ensure the MySQL service is running and the eight databases above exist.

### 2. Start the backend (order matters)

Start infrastructure first, then business services, then the gateway:

```
eureka-server (8761)
        ↓
config-server (8888)
        ↓
identity • asset • scheduling • demand-response • outage • billing • workorder • notification
        ↓
api-gateway (9090)
```

**From IntelliJ:** run each module's `*Application` main class in that order (Eureka, Config, gateway, and business services each have a Spring Boot run configuration).

**From the command line (Windows PowerShell), per service:**

```powershell
# Example for asset-service — run from energygrid-microservices/
Start-Process cmd.exe -ArgumentList '/c "asset-service\mvnw.cmd" -f "asset-service\pom.xml" -DskipTests spring-boot:run > "asset-service\run.log" 2>&1' -WindowStyle Hidden
```

> **Tip:** `mvn clean` frequently fails with a file lock on `target/` on Windows — skip `clean` and just use `spring-boot:run` (or `compile`).

Verify everything registered by opening the Eureka dashboard at **http://localhost:8761**.

### 3. Start the frontend

```powershell
cd EnergyGrid
npm install
npm start          # ng serve → http://localhost:4200
```

The app talks to the API Gateway at `http://localhost:9090`.

---

## 🔌 Service Ports Reference

| Service | Port |
|---|---|
| Eureka Server | 8761 |
| Config Server | 8888 |
| API Gateway | 9090 |
| identity-service | 8081 |
| asset-service | 8082 |
| scheduling-service | 8083 |
| demand-response-service | 8084 |
| outage-service | 8085 |
| billing-service | 8086 |
| workorder-service | 8087 |
| notification-service | 8088 |
| Angular frontend | 4200 |

---

## 📡 API Reference

All routes are reached through the **API Gateway** at `http://localhost:9090`. Selected endpoints:

### Identity & Admin (8081)
| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Log in, returns JWT |
| `POST` | `/api/auth/forget-password` | Request password reset |
| `POST` | `/api/auth/reset-password` | Reset password with token |
| `POST` | `/api/auth/forget-username` | Recover username |
| `GET`  | `/api/admin/users/pending` | List users awaiting approval |
| `PUT`  | `/api/admin/users/{id}/approve` | Approve a user |
| `DELETE`| `/api/admin/users/{id}` | Soft-delete a user |
| `PUT`  | `/api/admin/users/{id}/restore` | Restore a soft-deleted user |

### Assets & Telemetry (8082)
| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/assets/create` | Register an asset |
| `PUT`  | `/api/assets/put/{assetId}` | Update an asset |
| `DELETE`| `/api/assets/delete/{assetId}` | Remove an asset |
| `GET`  | `/api/assets/{assetId}` | Get an asset |
| | `/api/telemetry/**` | Telemetry ingestion/query |

### Scheduling & Dispatch (8083)
| Method | Path | Purpose |
|---|---|---|
| `GET`  | `/api/schedules/{id}` | Get a generation schedule |
| `GET`  | `/api/schedules/asset/{assetId}` | Schedules for an asset |
| `PUT`  | `/api/schedules/{id}/cancel` | Cancel a schedule |
| `GET`  | `/api/dispatch/{id}` | Get a dispatch record |
| `GET`  | `/api/dispatch/schedule/{scheduleId}` | Dispatches for a schedule |

### Demand Response (8084)
| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/demand-response/programs/create` | Create a DR program |
| `POST` | `/api/demand-response/events/create` | Create a DR event |
| `PATCH`| `/api/demand-response/events/{id}/activate` · `/complete` · `/cancel` | Move event lifecycle |
| `POST` | `/api/demand-response/participation/join` | Enrol in an event |
| `PATCH`| `/api/demand-response/participation/{id}/report` · `/verify` · `/opt-out` | Report/verify participation |

### Outages & Incidents (8085)
| Method | Path | Purpose |
|---|---|---|
| `GET`/`PATCH`/`DELETE` | `/api/outages/{id}` | Get / update / delete an outage |
| `GET`  | `/api/incident-tasks/{id}` | Get an incident task |
| `PUT`  | `/api/incident-tasks/{id}/status` | Update task status |
| `PUT`  | `/api/incident-tasks/{id}/evidence` | Attach evidence |

### Billing, Invoices & Payments (8086)
| Method | Path | Purpose |
|---|---|---|
| `GET`  | `/api/billing/{customerId}` | Billing summary for a customer |
| `GET`  | `/api/invoices/{id}` · `/customer/{customerId}` | Get invoice(s) |
| `PATCH`| `/api/invoices/{id}/status` | Update invoice status |
| `GET`  | `/api/payments/customer/{customerId}` · `/invoice/{invoiceId}` | Payment history |

### Work Orders & Evidence (8087)
| Method | Path | Purpose |
|---|---|---|
| `GET`/`PUT`/`DELETE` | `/api/work-orders/{id}` | Manage a work order |
| `PUT`  | `/api/work-orders/{id}/assign` | Assign to a technician |
| `PATCH`| `/api/work-orders/{id}/status` | Update status |
| `POST` | `/api/upload` | Upload an evidence file (multipart) |
| `GET`  | `/api/upload/{filename}` | Download an evidence file |
| `GET`/`PUT`/`DELETE` | `/api/evidence/{id}` | Manage evidence metadata |

### Notifications (8088)
| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/notifications/create` | Create a notification |
| `GET`  | `/api/notifications/user/{userId}` | List a user's notifications |
| `PUT`  | `/api/notifications/{id}/read` | Mark one as read |
| `PUT`  | `/api/notifications/read-all` | Mark all as read |

### Audit (federated)
`/api/identity/audit/{userId}` and `/api/audit/{asset|scheduling|outage|workorder|billing|dr|notification}` — each service exposes its own audit trail; the admin UI merges them.

---

## 🔐 Cross-Cutting Features

- **JWT authentication** issued by identity-service and validated by each service via a security filter.
- **Role-based access control** across the six roles.
- **Audit logging** — every meaningful action is recorded to an `audit_log` table in the owning service and exposed via `/api/audit/**`.
- **Notifications** — services raise notifications (e.g. work-order assignment, outage declared) that customers/technicians see in-app.
- **Soft deletes** — users and key records are flagged deleted rather than removed, preserving the audit trail.
- **Centralised config & discovery** via Config Server and Eureka.
- **File uploads** — maintenance evidence is uploaded through the gateway (multipart, up to ~12 MB) and stored under `uploads/`.

---

## 🗂 Project Layout

```
EnergyGridNew/
├── EnergyGrid/                       # Angular 21 frontend
│   ├── src/
│   ├── package.json
│   └── angular.json
├── energygrid-microservices/         # Spring Boot backend (11 modules)
│   ├── eureka-server/                # Service discovery        (8761)
│   ├── config-server/                # Centralised config       (8888)
│   ├── api-gateway/                  # Edge routing             (9090)
│   ├── identity-service/             # Auth & users             (8081)
│   ├── asset-service/                # Assets & telemetry       (8082)
│   ├── scheduling-service/           # Schedules & dispatch     (8083)
│   ├── demand-response-service/      # DR programs & events     (8084)
│   ├── outage-service/               # Outages & incidents      (8085)
│   ├── billing-service/              # Billing, invoices, pay   (8086)
│   ├── workorder-service/            # Work orders & evidence   (8087)
│   ├── notification-service/         # Notifications            (8088)
│   └── uploads/                      # Uploaded evidence files
└── README.md
```

Each backend module follows the standard layout: `controller/ → service/ → repository/ → entity/`, with `dto/`, `config/`, and security filters alongside.

---

## 🩺 Troubleshooting

| Symptom | Cause & Fix |
|---|---|
| Service won't start, "Unknown database" | The MySQL database for that service doesn't exist yet — run the `CREATE DATABASE` script above. |
| `identity-service` hangs on `/actuator/health` | Its mail health indicator opens an SMTP connection to Gmail on every health check and hangs when outbound SMTP is blocked. Set `management.health.mail.enabled=false` in its local `application.properties`. |
| `config-server` health looks blank | The Config Server's `/actuator/health` returns a config document, not a health JSON. Check the **Eureka dashboard** (`:8761`) for its real status instead. |
| `mvn clean` fails with a file lock on `target/` | Common on Windows. Skip `clean`; just run `compile` / `spring-boot:run`. |
| Services don't appear in Eureka | Make sure `eureka-server` (8761) and `config-server` (8888) are up **before** the business services. |
| Frontend gets CORS / 404 errors | Confirm the API Gateway (9090) is running — the frontend routes everything through it. |
| `mysql` command not found | `mysql.exe` isn't on PATH; use the full path `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`. |

---

*Built as a learning project demonstrating Spring Cloud microservices, JWT security, database-per-service, and an Angular SPA front end.*
