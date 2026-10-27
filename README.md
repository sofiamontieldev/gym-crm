# Gym CRM REST API

Spring Boot REST API for managing trainee profiles, trainer profiles and trainings. It includes HTTP Basic authentication, environment-specific database profiles, Actuator health indicators, Prometheus metrics, correlated request logging and automated coverage verification.

## Technology stack

- Java 21 and Spring Boot 3.4.0
- Spring MVC, Bean Validation and Spring Security
- Hibernate ORM and Spring transactions
- H2 databases for development, tests and the standalone production profile
- MySQL 8.4 for the Docker profile
- ActiveMQ Classic events and a REST client for `gym-reports`
- Actuator and Micrometer Prometheus registry
- JUnit 5, Mockito, REST Assured and JaCoCo
- Maven

## Before you start

There are two ways to run this repository:

- **Main application only:** use the `dev` profile and the in-memory H2 database. This is useful for working only on `gym-crm`.
- **Complete solution:** use Docker Compose to start `gym-crm`, `gym-reports`, MySQL, MongoDB and ActiveMQ together. This is the recommended option for reviewing the containerization task.

The Main-only option does not require an external database.

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
| `docker` | `application-docker.properties` | MySQL service `mysql` | `update` | Disabled | Set by `compose.yaml` |

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
- `/actuator/health` and `/actuator/info` are public for container orchestration.
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
| `GET` | `/api/v1/reports/trainers/{username}/workload` | Active user, Docker profile | Proxy a workload query to `gym-reports` |

Training filters use ISO dates (`yyyy-MM-dd`). `trainerName` and `traineeName` refer to usernames.

## Observability

`/actuator/health` and `/actuator/info` are public. `/actuator/metrics` and
`/actuator/prometheus` remain protected.

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
                          +-> after-commit ActiveMQ events -> gym-reports -> MongoDB

REST reports endpoint -> ReportsClient -> gym-reports
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

## Run the complete solution with Docker

Docker Compose builds the two applications and starts these five containers:

- `main`: the `gym-crm` API;
- `reports`: the `gym-reports` API;
- `mysql`: the database used by `gym-crm`;
- `mongodb`: the database used by `gym-reports`;
- `activemq`: the message broker used to send events from Main to Reports.

### 1. Check the folder structure

The two repositories must be separate sibling folders. Do not place `gym-reports`
inside `gym-crm`.

```text
epam-sp/
|-- gym-crm/
`-- gym-reports/
```

The `compose.yaml` file is inside `gym-crm` and uses `../gym-reports` to find and
build the Reports project.

### 2. Check the requirements

Install and start Docker Desktop. Wait until Docker Desktop reports that the
engine is running. Then open PowerShell and verify:

```powershell
docker version
docker compose version
```

`docker version` must show both a **Client** section and a **Server** section.

### 3. Create the local environment file

Open PowerShell in the `gym-crm` folder. For example:

```powershell
cd C:\path\to\epam-sp\gym-crm
```

Replace `C:\path\to` with the location where the repositories were downloaded.
Before continuing, the PowerShell prompt should end in `\gym-crm`.

Copy the example environment file. This command is needed only the first time:

```powershell
Copy-Item .env.example .env
```

The new `.env` file provides the local usernames, passwords and database names
used by Docker Compose. The included values are only for this local academic
project. The `.env` file is ignored by Git and must not be committed.

### 4. Build and start the five containers

Run these commands from the `gym-crm` folder:

```powershell
docker compose config --quiet
docker compose up --build -d
```

The first execution can take several minutes because Docker must download the
base images, build both Java applications and create the databases. The `-d`
option leaves the containers running in the background.

### 5. Check that the containers are ready

Run:

```powershell
docker compose ps
```

The output must list `main`, `reports`, `mysql`, `mongodb` and `activemq`.
`main`, `reports`, `mysql` and `mongodb` should show `healthy`. ActiveMQ does not
have a Compose health check, so its normal state is `Up`.

If a service still says `starting`, wait a few seconds and run
`docker compose ps` again.

### 6. Check the two applications

Run:

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8081/actuator/health
```

Both responses must contain `"status":"UP"`. This confirms that the two Spring
Boot applications started and can reach their required services.

The available addresses are:

| Component | Address |
|---|---|
| Main API | `http://localhost:8080` |
| Main Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Reports API | `http://localhost:8081` |
| ActiveMQ console | `http://localhost:8161` |
| MongoDB from Windows | `localhost:27018` |

The ActiveMQ console uses `admin` as both username and password in this local
setup. Protected Main endpoints and Swagger operations require an application
user created through `POST /api/v1/trainees` or `POST /api/v1/trainers`, as
explained in the Quick start section.

The cross-service JSON contract is documented in
[`docs/contracts/trainer-workload-v1.md`](docs/contracts/trainer-workload-v1.md).

### 7. Review logs if a container does not start

Show the latest logs from both applications:

```powershell
docker compose logs --tail 50 main reports
```

To follow the logs while testing the solution, use:

```powershell
docker compose logs -f main reports
```

Press `Ctrl+C` to stop following the logs. This does not stop the containers.

### 8. Stop the solution

Stop and remove the containers without deleting the database data:

```powershell
docker compose down
```

Do not use `docker compose down -v` unless you intentionally want to delete the
MySQL and MongoDB data. The next time, start the solution with:

```powershell
docker compose up -d
```

## Troubleshooting

### Docker cannot connect to the Docker API

Start Docker Desktop and wait until its engine is running. Then run
`docker version` again. Do not continue until the command shows both **Client**
and **Server** information.

### A container is unhealthy or did not start

Check its current state and the latest application logs:

```powershell
docker compose ps
docker compose logs --tail 50 main reports
```

After correcting the reported problem, start the services again with
`docker compose up -d`.

### A required port is already in use

This solution uses ports `8080`, `8081`, `8161`, `61616` and `27018` on the
computer. Stop the other application or container that is already using the
reported port, and then run `docker compose up -d` again.

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
