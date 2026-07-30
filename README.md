# MedSys Appointment Module

A clinic appointment booking service built for the **MedSys Solutions** case study.

Spring Boot 3.3 REST service on Java 17, built with Maven. The repository demonstrates a
Scrum + DevOps workflow in which every change travels from a tracked requirement through
automated verification and human review to a versioned release.

---

## Business rules

The service enforces three rules on every booking request:

1. **All fields are mandatory** — `doctorId`, `patientName` and `time` must be present.
2. **No past-dated appointments** — the requested time must be in the future.
3. **No double-booking** — a doctor cannot hold two active appointments in the same slot.
   A cancelled appointment releases its slot.

---

## API

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/appointments` | Book an appointment |
| `DELETE` | `/api/appointments/{id}` | Cancel an appointment |
| `GET` | `/api/appointments` | List all appointments |

Every response uses a uniform envelope so that clients handle success and business-rule
rejections consistently:

```json
{
  "code": 1,
  "message": "Appointment booked successfully",
  "resultData": {
    "id": 1,
    "doctorId": 10,
    "patientName": "Alice Tan",
    "time": "2026-08-01T09:30:00",
    "status": "BOOKED"
  }
}
```

`code` is `1` for success and `0` for a business-rule rejection. Rejections return HTTP 200
with an explanatory `message`; the transport succeeded, the request was refused.

---

## Project structure

```
com.medsys.appointment
├── AppointmentApplication.java      Spring Boot entry point
├── entity/
│   ├── Appointment.java             Domain object (Lombok)
│   ├── AppointmentStatus.java       BOOKED | CANCELLED
│   └── ResultRet.java               Uniform response envelope
├── service/
│   └── AppointmentService.java      Business rules
└── controller/
    └── AppointmentController.java   REST endpoints
```

---

## Build and verify

```bash
mvn -B verify
```

One command runs the full verification chain, and it is the same command GitHub Actions
executes on every push and pull request:

| Stage | Tool | Gate |
|---|---|---|
| Compile | Maven | — |
| Unit and integration tests | JUnit 5 | 11 tests must pass |
| Coverage | JaCoCo | line ≥ 70%, branch ≥ 60% |
| Static analysis | SpotBugs | no unsuppressed findings |
| Package | Maven | `target/medsys-appointment-1.0.0.jar` |

Any failing stage returns a non-zero exit code, turns the required check red and blocks the
pull request from being merged.

**Current measurements:** 11 tests passing (8 service-level, 3 MockMvc), 95.2% line coverage,
81.8% branch coverage, zero SpotBugs findings.

Build artefacts:

| Artefact | Path |
|---|---|
| Coverage report | `target/site/jacoco/index.html` |
| Static analysis result | `target/spotbugsXml.xml` (view with `mvn spotbugs:gui`) |
| Test detail | `target/surefire-reports/` |
| Deliverable | `target/medsys-appointment-1.0.0.jar` |

---

## Suppression policy

`spotbugs-exclude.xml` holds the SpotBugs suppressions. Every entry records the pattern
suppressed, its scope, why the finding is not actionable, and who reviewed it. A suppression
is a reviewed engineering decision, not a way of clearing the dashboard — new entries require
approval in the pull request.

---

## API acceptance tests

`postman/MedSys-Appointment-API.postman_collection.json` verifies the three business rules
from a consumer's perspective. Import it into Postman and use **Run collection**:

| # | Request | Asserts |
|---|---|---|
| 1 | Valid booking succeeds | `code=1`, confirmation message, `status=BOOKED` |
| 2 | Double booking rejected | `code=0`, message names the conflicting doctor |
| 3 | Past-dated booking refused | `code=0`, documented rejection message |

The first request generates a fresh future slot and the second reuses it, so the collection
produces the same result on every run. It is versioned alongside the service precisely so the
acceptance examples cannot drift away from the API.

---

## Running locally

```bash
mvn spring-boot:run
```

The service listens on port 8080.

---

## Scope and assumptions

- **Storage is in memory.** Data resets when the application restarts. A production system
  would substitute a persistent repository behind the same service interface.
- **All data is synthetic.** No patient information enters this repository, the Postman
  collection or the CI logs.
- **This repository is educational** and is public so that it can be assessed. A MedSys
  production repository would use organisational access controls.
- **A GitHub Release is a delivery record, not a deployment.** Clinical deployment, training,
  rollback planning and post-release monitoring remain controlled operational activities
  outside the scope of this demonstration.
