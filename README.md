# Appointment Booking System

REST backend for any appointment-based business: clinics, salons, tutors, turf/court booking.

**Stack:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, PostgreSQL (H2 for local dev), Swagger UI, Docker, GitHub Actions.

## Features
- Register / login with JWT, role-based access (`USER`, `ADMIN`)
- Admin manages services (name, duration, price)
- Live slot availability per service and day (working hours configurable)
- **Double-booking prevention** using a pessimistic row lock and an overlap check inside one transaction
- Customers book, list and cancel their own appointments; admin sees the day's schedule
- Validation, consistent JSON errors, OpenAPI docs

## Run
```bash
mvn spring-boot:run            # in-memory H2, ready in seconds
# or with PostgreSQL
docker compose up --build
```
Swagger UI: http://localhost:8080/swagger-ui.html
Default admin: `admin@example.com` / `admin123` (override with `ADMIN_EMAIL`, `ADMIN_PASSWORD`).

## Quick tour
```bash
# 1. register
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Asha","email":"asha@example.com","password":"secret1"}' | jq -r .token)

# 2. see services and free slots
curl localhost:8080/api/services
curl "localhost:8080/api/services/1/slots?date=2030-01-15"

# 3. book
curl -X POST localhost:8080/api/appointments -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"serviceId":1,"startTime":"2030-01-15T10:00:00"}'
```

## API
| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/services`, `/api/services/{id}/slots?date=` | public |
| POST | `/api/services` | admin |
| POST | `/api/appointments` | user |
| GET | `/api/appointments/mine` | user |
| DELETE | `/api/appointments/{id}` (cancel) | owner or admin |
| GET | `/api/appointments/admin?date=` | admin |

## Ideas to extend
Email/SMS reminders (scheduled job), multiple staff members, payments, Google Calendar sync, React frontend.

## Demo
Live demo: _add your Render / Railway link here_
