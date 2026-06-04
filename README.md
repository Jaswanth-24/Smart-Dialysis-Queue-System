# 🏥 Smart Dialysis Queue System

> A production-ready backend system for managing dialysis center operations — from patient check-in to machine assignment, session tracking, and real-time queue broadcasting.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?style=flat-square&logo=springsecurity)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-purple?style=flat-square)
![H2](https://img.shields.io/badge/Database-H2%20%2F%20MySQL-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## 📌 Problem Statement

Dialysis patients require life-critical sessions 3 times a week. Most dialysis centers manage queues manually using paper or spreadsheets — leading to chaotic check-ins, untracked machine availability, and no visibility for staff or patients.

This system digitizes and automates the entire workflow — from the moment a patient walks in to the moment a machine is cleaned and reassigned to the next patient.

---

## ✨ Key Highlights

- 🤖 **Auto Machine Assignment** — When a patient checks in, the system instantly finds and reserves an available machine. No manual assignment needed.
- ⏱️ **Smart ETA Calculation** — Live queue shows estimated wait time based on active machine count and average dialysis duration.
- 📡 **Real-Time WebSocket Broadcast** — Every state change (check-in, session start, session complete) is pushed instantly to all connected clients via STOMP.
- 🔄 **Automated Machine Release** — A scheduler runs every 10 seconds (configurable), detects machines that finished cleaning, and automatically assigns them to the next waiting patient.
- 🛡️ **Role-Based Access Control** — 5 distinct roles (ADMIN, RECEPTIONIST, TECHNICIAN, NURSE, DOCTOR) with endpoint-level authorization.
- 🔁 **Re-Check-In After Cancellation** — Cancelled patients can re-join the queue on the same day — other systems block this incorrectly.
- 👨‍⚕️ **Technician Attribution** — Every dialysis session records which technician started it, extracted from the JWT token automatically.

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                         │
│   Receptionist App    Technician App    Public Display      │
└──────────────┬──────────────┬──────────────────┬───────────┘
               │   REST API   │                  │ WebSocket
               ▼              ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                     SPRING BOOT BACKEND                     │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Auth   │  │  Queue   │  │ Session  │  │ Machine  │   │
│  │ Controller│  │Controller│  │Controller│  │Controller│   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │              │              │              │         │
│  ┌────▼──────────────▼──────────────▼──────────────▼─────┐  │
│  │              SERVICE LAYER                             │  │
│  │  AuthService  QueueService  SessionService             │  │
│  │  MachineService  MachineSchedulerService               │  │
│  └────────────────────────┬───────────────────────────────┘  │
│                           │                                  │
│  ┌────────────────────────▼───────────────────────────────┐  │
│  │              REPOSITORY LAYER (JPA)                    │  │
│  └────────────────────────┬───────────────────────────────┘  │
│                           │                                  │
│  ┌────────────────────────▼───────────────────────────────┐  │
│  │              H2 DATABASE (dev) / MySQL (prod)          │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │   QueueBroadcaster (WebSocket / STOMP)              │   │
│  │   /topic/queue/{centerId}                           │   │
│  │   /topic/machine/{centerId}                         │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Core Flow

```
Patient arrives at center
        │
        ▼
Receptionist: POST /api/queue/check-in
        │
        ├── Machine AVAILABLE? ──YES──► Status: ASSIGNED
        │                               Machine: RESERVED (IN_USE)
        │                               WebSocket broadcast fired
        └── No machine free? ──────────► Status: WAITING
                                          (scheduler will assign when one frees)
        │
        ▼
Technician: POST /api/sessions/{queueId}/start
        │
        ▼
Status: IN_SESSION ─── DialysisSession created ─── Technician recorded
        │
        ▼
Technician: PATCH /api/sessions/{sessionId}/complete
        │
        ▼
Status: COMPLETED ─── Machine: CLEANING ─── availableAt = now + 20min
        │
        ▼
MachineSchedulerService (runs every 10s)
        │
        ▼
availableAt passed? ──YES──► Machine: AVAILABLE
                              Next WAITING patient? ──YES──► ASSIGNED
                              WebSocket broadcast fired ◄──────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | H2 (dev) / MySQL-ready (prod) |
| ORM | Spring Data JPA + Hibernate 6 |
| Real-time | WebSocket + STOMP (SockJS) |
| Schema | Flyway (ready, disabled in dev) |
| Build | Maven |
| Boilerplate | Lombok |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

---

## 📁 Project Structure

```
src/main/java/com/dialysis/
│
├── config/
│   ├── DataSeeder.java          # Seeds test data on startup
│   ├── SecurityConfig.java      # JWT filter chain + CORS
│   ├── SwaggerConfig.java       # OpenAPI / Swagger setup
│   └── WebSocketConfig.java     # STOMP broker config
│
├── controller/
│   ├── AuthController.java      # Login
│   ├── StaffController.java     # Register staff (ADMIN only)
│   ├── PatientController.java   # Register patients
│   ├── MachineController.java   # Machine CRUD + status
│   ├── QueueController.java     # Check-in, live queue, cancel
│   ├── SessionController.java   # Start & complete sessions
│   └── PublicController.java    # Public queue display (no auth)
│
├── service/
│   ├── AuthService.java
│   ├── QueueService.java        # Core queue logic + ETA
│   ├── SessionService.java      # Machine assignment + session lifecycle
│   ├── MachineService.java      # Machine management
│   ├── MachineSchedulerService.java  # Auto-release after cleaning
│   ├── PatientService.java
│   └── StaffService.java
│
├── entity/          # JPA entities: User, Patient, Center, Machine,
│                    # QueueEntry, DialysisSession, SessionValues,
│                    # DoctorConsultation, TestReport
│
├── enums/           # Role, QueueStatus, MachineStatus,
│                    # SessionStatus, ConsultationStatus, TestType
│
├── repository/      # Spring Data JPA repositories
├── security/        # JwtAuthFilter
├── websocket/       # QueueBroadcaster
├── dto/             # Request/Response records
├── exception/       # GlobalExceptionHandler, custom exceptions
└── util/            # JwtUtil
```

---

## 🔐 API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/login` | Public | Login and receive JWT token |
| `POST` | `/api/auth/admin/register` | `ADMIN` | Register staff member |
| `POST` | `/api/auth/patient/register` | `RECEPTIONIST` | Register new patient |

### Machines

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/machines` | `RECEPTIONIST` | Add machine to center |
| `GET` | `/api/machines/center/{centerId}` | Staff | List all machines |
| `GET` | `/api/machines/{machineId}` | Staff | Get single machine |
| `PATCH` | `/api/machines/{id}/status?status=` | `RECEPTIONIST`, `TECHNICIAN` | Change machine status |
| `PATCH` | `/api/machines/{id}/deactivate` | `RECEPTIONIST` | Retire a machine |
| `PATCH` | `/api/machines/{id}/activate` | `RECEPTIONIST` | Restore a machine |

### Queue

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/queue/check-in` | `RECEPTIONIST` | Check patient in — auto-assigns machine |
| `GET` | `/api/queue/live/{centerId}` | Staff | Live queue with ETA |
| `PATCH` | `/api/queue/{id}/cancel` | `RECEPTIONIST`, `TECHNICIAN` | Cancel a queue entry |
| `GET` | `/api/public/live/{centerId}` | **Public** | Queue display for waiting room |

### Sessions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/sessions/{queueId}/start` | `TECHNICIAN` | Start dialysis session |
| `PATCH` | `/api/sessions/{sessionId}/complete` | `TECHNICIAN` | Complete session + release machine |

---

## 📡 WebSocket Events

Connect via SockJS at `ws://localhost:8080/ws`, subscribe using STOMP.

| Topic | Payload | Triggered when |
|-------|---------|----------------|
| `/topic/queue/{centerId}` | `List<LiveQueueResponse>` | Any queue state change |
| `/topic/machine/{centerId}` | Machine status | Machine assigned or released |

**LiveQueueResponse shape:**
```json
{
  "tokenNumber": 3,
  "patientName": "Sai Kumar",
  "status": "ASSIGNED",
  "assignedMachine": "M-01",
  "estimatedWaitMinutes": 0
}
```

---

## 👥 Roles & Permissions

| Role | Capabilities |
|------|-------------|
| `ADMIN` | Register any staff, system configuration |
| `RECEPTIONIST` | Register patients, check-in, manage machines, cancel queue |
| `TECHNICIAN` | Start/complete sessions, update machine status, cancel queue |
| `NURSE` | View live queue, view machines |
| `DOCTOR` | View live queue |
| `PATIENT` | (Phase 2 — patient portal) |

---

## ⚙️ Queue Status Lifecycle

```
WAITING ──► ASSIGNED ──► IN_SESSION ──► COMPLETED
   │            │
   └────────────┴──► CANCELLED
```

| Status | Meaning |
|--------|---------|
| `WAITING` | Checked in, no machine available yet |
| `ASSIGNED` | Machine reserved, waiting for technician |
| `IN_SESSION` | Dialysis actively in progress |
| `COMPLETED` | Session finished |
| `CANCELLED` | Patient left or removed (can re-check-in same day) |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run locally

```bash
git clone https://github.com/Jaswanth-24/Smart-Dialysis-Queue-System.git
cd Smart-Dialysis-Queue-System
mvn spring-boot:run
```

On startup, **DataSeeder** automatically inserts test data and prints credentials to the console:

```
╔══════════════════════════════════════════════════════════════╗
║              DIALYSIS SEEDER — TEST DATA READY              ║
╠══════════════════════════════════════════════════════════════╣
║  CENTER
║  Apollo Center ID:   <uuid>
╠══════════════════════════════════════════════════════════════╣
║  STAFF — all passwords: password123
║  ADMIN:         admin@dialysis.com
║  RECEPTIONIST:  receptionist@dialysis.com
║  TECHNICIAN:    technician@dialysis.com
║  NURSE:         nurse@dialysis.com
║  DOCTOR:        doctor@dialysis.com
╠══════════════════════════════════════════════════════════════╣
║  PATIENTS — all passwords: patient123
║  Sai Kumar    →  sai@patient.com
║  Priya Reddy  →  priya@patient.com
║  Ravi Sharma  →  ravi@patient.com
╚══════════════════════════════════════════════════════════════╝
```

### Access points

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8080/h2-console` | Database browser |
| `ws://localhost:8080/ws` | WebSocket endpoint |

---

## 🔧 Configuration

```properties
# Machine cleaning duration (1 min dev, 20 min prod)
app.machine.cleaning-minutes=1

# Scheduler check interval (10s dev, 60s prod)
app.machine.scheduler-interval-ms=10000

# JWT secret (override in production via environment variable)
jwt.secret=${JWT_SECRET:dev-fallback-secret}
jwt.expiration-ms=86400000        # 24 hours for staff
jwt.patient-expiration-ms=604800000  # 7 days for patients
```

---

## 🗄️ Database Schema

```
centers ──────────────────────────────────────────────────────┐
    │                                                          │
    ├── users (ADMIN, RECEPTIONIST, TECHNICIAN, NURSE, DOCTOR)│
    │                                                          │
    ├── patients ──── user (1:1)                               │
    │       └── doctor (user with DOCTOR role)                 │
    │                                                          │
    ├── machines ─────────────────────────────────────────────┘
    │       └── status: AVAILABLE → ASSIGNED → IN_USE
    │                   → CLEANING → AVAILABLE (cycle)
    │
    └── queue_entries
            └── dialysis_sessions
                    └── session_values (clinical measurements)
                    └── doctor_consultations (Phase 2)
                    └── test_reports (Phase 2)
```

---

## 🧪 Testing the Full Flow

1. **Login as receptionist** → `POST /api/auth/login`
2. **Check in patient** → `POST /api/queue/check-in` → machine auto-assigned → status `ASSIGNED`
3. **View live queue** → `GET /api/public/live/{centerId}` → see token, machine number, status
4. **Login as technician** → `POST /api/auth/login`
5. **Start session** → `POST /api/sessions/{queueId}/start` → status `IN_SESSION`
6. **Complete session** → `PATCH /api/sessions/{sessionId}/complete` → machine enters `CLEANING`
7. **Wait ~1 minute** (dev setting) → scheduler fires → machine `AVAILABLE` → next patient auto-assigned

---

## 🗺️ Phase 2 Roadmap

- [ ] **Doctor consultation workflow** — auto-trigger after every 10 sessions
- [ ] **Clinical session values** — blood pressure, weight, UF rate, complications
- [ ] **Test report upload** — RFT, CBP, KFT, serum electrolytes
- [ ] **Patient portal** — patients see their queue position and session history
- [ ] **Priority queue engine** — age + wait time weighted scoring for fair assignment
- [ ] **MySQL migration** — production-ready database with Flyway migrations
- [ ] **Docker + docker-compose** — one-command deployment
- [ ] **CI/CD pipeline** — GitHub Actions

---

---

*Built as a real-world clinical system demonstrating Spring Boot best practices — role-based security, WebSocket real-time updates, scheduler-based automation, and clean layered architecture.*
