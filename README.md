# Clinic backend

Spring Boot REST API for the clinic assignment. It uses PostgreSQL and listens
on port `8081` by default.

## Requirements

- Java 21
- PostgreSQL

Create the `clinicsystem_db` database, then set the database password before
starting the application. In PowerShell:

```powershell
$env:DB_PASSWORD = "your-local-database-password"
.\mvnw.cmd spring-boot:run
```

Optional connection settings:

- `DB_URL` (defaults to `jdbc:postgresql://localhost:5432/clinicsystem_db`)
- `DB_USERNAME` (defaults to `postgres`)
- `DB_PASSWORD` (required)

The source is under `src/main/java`. Generated build output and local
configuration should not be committed.
