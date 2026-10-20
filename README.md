# Gym CRM REST API

Spring Boot REST API for managing trainee profiles, trainer profiles and trainings. It includes HTTP Basic authentication, environment-specific database profiles, Actuator health indicators, Prometheus metrics, correlated request logging and automated coverage verification.

## Technology stack

- Java 21 and Spring Boot 3.4.0
- Spring MVC, Bean Validation and Spring Security
- Hibernate ORM and Spring transactions
- H2 in-memory databases for development and tests
- H2 file database for the persistent production profile
- Actuator and Micrometer Prometheus registry
- JUnit 5, Mockito, REST Assured and JaCoCo
- Maven

## Before you start

The recommended reviewer path uses the `dev` profile and an in-memory H2 database, so it does not require an external database.

Install:

1. JDK 21.
2. Maven 3.9 or newer.

Verify with:

```text
java -version
mvn -version
```

Both commands must report Java 21. Run the following commands from the project root, where `pom.xml` is located.

## Quick start

### 1. Build and verify

```text
mvn clean verify
```

The tests automatically use the isolated `test` profile and do not require an external database or environment variables.

### 2. Start the application

```text
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Wait until the logs report that the `dev` profile is active and contain `Started Application`. The API is available at `http://localhost:8080`. Keep this terminal open and use `Ctrl+C` to stop the application.

### 3. Register the first user

Only trainee and trainer registration are public. Register an account before opening Swagger, Actuator or the H2 console because those resources require authentication.

PowerShell example:

```powershell
$trainee = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/trainees" `
  -ContentType "application/json" `
  -Body (@{
    firstName = "Review"
    lastName = "User"
    dateOfBirth = "1990-01-15"
    address = "Bogota"
  } | ConvertTo-Json)

$trainee
```

The response contains a generated username and a 10-character password. Copy both values because the plain-text password is returned only at registration. A trainer can be registered through `POST /api/v1/trainers` using one of these specializations: `FITNESS`, `YOGA`, `ZUMBA`, `STRETCHING` or `RESISTANCE`.

### 4. Verify authentication and health

Replace the example credentials with the values returned by registration:

```powershell
curl.exe --user "Review.User:generatedPassword" `
  "http://localhost:8080/api/v1/auth/login"

curl.exe --user "Review.User:generatedPassword" `
  "http://localhost:8080/actuator/health"
```

Login should return `{"message":"Authenticated"}` and health should report `"status":"UP"`.

### 5. Open Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The browser will request HTTP Basic credentials. Enter the generated application credentials. In Swagger UI, select **Authorize** and use the same values for protected operations.

## Spring profiles and databases

`application.properties` contains only configuration shared by all environments. Database connections are defined by profile:

| Profile | Configuration file | Database | Schema strategy | H2 console | Activation |
|---|---|---|---|---|---|
| `dev` | `application-dev.properties` | In-memory H2 `gymcrm-dev` | `create-drop` | Enabled | `--spring.profiles.active=dev` |
| `test` | `application-test.properties` | In-memory H2 `gymcrm-test` | `create-drop` | Disabled | `@ActiveProfiles("test")` in Spring tests |
| `prod` | `application-prod.properties` | H2 file `data/gymcrm-prod` | `update` | Disabled | `SPRING_PROFILES_ACTIVE=prod` |

The development and test databases use different JDBC URLs and are removed when their respective Spring contexts stop. A runtime profile must be selected explicitly when starting the application.

### Development H2 console

With the `dev` profile running, open:

```text
http://localhost:8080/h2-console
```

First authenticate with the generated application credentials if the browser requests them. Then enter these values in the H2 form:

| H2 field | Value |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:gymcrm-dev;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | Leave empty |

The JDBC URL must match exactly. Because `dev` uses `create-drop`, all development data is removed when the application stops.

### Production profile

The `prod` profile uses a persistent H2 file database. It does not require an external database installation, creates missing tables with `spring.jpa.hibernate.ddl-auto=update` and preserves data between executions. The generated database files are stored under `data/`, which is excluded from Git.

Build and start it with PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"

java -jar target/gym-crm-1.0-SNAPSHOT.jar
```

In Bash, run `export SPRING_PROFILES_ACTIVE=prod` before starting the JAR.

## Authentication and authorization

The application is stateless and uses HTTP Basic authentication.

- `POST /api/v1/trainees` and `POST /api/v1/trainers` are public.
- Every other API, Swagger, H2 and Actuator request is protected.
- Credentials are checked against the stored username and BCrypt password hash.
- Profile operations require the authenticated user to match the target `{username}`.
- An inactive user cannot perform normal operations but can reactivate their own account.
- Registration responses use `Cache-Control: no-store` because they contain the one-time plain-text password.

## Main endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/trainees` | Public | Register a trainee and return credentials |
| `POST` | `/api/v1/trainers` | Public | Register a trainer and return credentials |
| `GET` | `/api/v1/auth/login` | Authenticated user | Validate credentials and active status |
| `PUT` | `/api/v1/auth/password` | Active account owner | Change the authenticated user's password |
| `GET` | `/api/v1/trainees/{username}` | Active account owner | Get a trainee profile |
| `PUT` | `/api/v1/trainees/{username}` | Active account owner | Update a trainee profile |
| `DELETE` | `/api/v1/trainees/{username}` | Active account owner | Delete a trainee and their trainings |
| `GET` | `/api/v1/trainers/{username}` | Active account owner | Get a trainer profile |
| `PUT` | `/api/v1/trainers/{username}` | Active account owner | Update a trainer profile |
| `GET` | `/api/v1/trainees/{username}/available-trainers` | Active account owner | List active, unassigned trainers |
| `PUT` | `/api/v1/trainees/{username}/trainers` | Active account owner | Replace a trainee's trainer list |
| `GET` | `/api/v1/trainees/{username}/trainings` | Active account owner | Get trainee trainings with optional filters |
| `GET` | `/api/v1/trainers/{username}/trainings` | Active account owner | Get trainer trainings with optional filters |
| `POST` | `/api/v1/trainings` | Active user | Create a training using the trainer specialization as its type |
| `PATCH` | `/api/v1/users/{username}/status` | Account owner | Activate or deactivate an account |
| `GET` | `/api/v1/training-types` | Active user | List the fixed training types |

Training filters use ISO dates (`yyyy-MM-dd`). `trainerName` and `traineeName` refer to usernames.

## Observability

The protected Actuator endpoints are `/actuator/health`, `/actuator/info`, `/actuator/metrics` and `/actuator/prometheus`.

### Health indicators

| Component | Verification |
|---|---|
| `gymDatabase` | Obtains and validates a database connection |
| `trainingCatalog` | Confirms that all five fixed training types exist |

An authenticated health response should include both custom components with status `UP`. Spring Boot can include additional components.

### Prometheus metrics

| Metric | Labels | Incremented when |
|---|---|---|
| `gym_profile_registrations_total` | `role=trainee|trainer` | A profile is registered |
| `gym_trainings_created_total` | `type=<training type>` | A training is created |
| `gym_password_changes_total` | `role=trainee|trainer` | A password is changed successfully |

Counters appear after their corresponding event occurs. Query the Prometheus endpoint with:

```powershell
curl.exe --silent --user "Review.User:generatedPassword" `
  "http://localhost:8080/actuator/prometheus" |
  Select-String "gym_profile_registrations|gym_trainings_created|gym_password_changes"
```

## Responses and logging

Registrations return `201 Created`; other successful business operations generally return `200 OK`. Errors use `application/problem+json`:

- `400 Bad Request`: validation failure;
- `401 Unauthorized`: missing or invalid credentials, or inactive user;
- `403 Forbidden`: attempt to operate on another account;
- `404 Not Found`: missing resource;
- `409 Conflict`: invalid or repeated account-status transition.

Every HTTP response includes `X-Transaction-Id`. The same identifier appears in console logs to correlate each request. Passwords and password hashes are not logged.

## Architecture

```text
REST controllers -> Services -> Hibernate DAOs -> Profile database
                          |
                          +-> authentication and business rules
                          +-> Micrometer metrics
```

- Spring dependencies use constructor injection.
- Controllers expose DTOs instead of Hibernate entities.
- Services own transactions and business rules.
- DAOs contain persistence queries.

## Tests and coverage

Run the complete quality gate:

```text
mvn clean verify
```

This runs unit and integration tests, packages the application, generates `target/site/jacoco/index.html` and fails when line coverage is below 80%.

Run tests without packaging or the final coverage check:

```text
mvn test
```

Tests cover domain and service behavior, persistence, Spring wiring, security, custom health indicators, metrics and the complete REST workflow.

## Troubleshooting

### Missing database properties at startup

Activate a runtime profile. For local review:

```text
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### `401 Unauthorized`

Register a trainee or trainer first, then use the generated credentials with HTTP Basic authentication.

### H2 shows no tables

Use exactly `jdbc:h2:mem:gymcrm-dev;DB_CLOSE_DELAY=-1` while the application is running with `dev`.

### Port 8080 is in use

Stop the process using the port or start on another one:

```text
mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.port=8081"
```
