# Gym CRM REST API

Spring Boot REST API for managing trainees, trainers and trainings with an H2 database.

## Requirements

- JDK 21
- Maven 3.9 or newer
- Any Java IDE with Maven support (optional)

Verify the installed tools with:

```bash
java -version
mvn -version
```

## Quick start

1. Clone or extract the project.
2. Open a terminal in the project root, where `pom.xml` is located.
3. Build and verify the project:

```bash
mvn clean verify
```

4. Start the application:

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

No external database installation is required. The application uses an in-memory H2 database and creates a new schema when it starts.

## API documentation

With the application running, open:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Swagger shows the available requests, response models, validation rules and HTTP Basic authentication configuration. It is also the easiest way to try the API manually.

## Inspecting the H2 database

The H2 console is enabled for local development. Keep the application running and open:

```text
http://localhost:8080/h2-console
```

Use these connection settings:

| Field | Value |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:gymcrm;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | Leave empty |

Select **Connect**. The JDBC URL must match exactly; a different URL can open a separate empty database.

Register trainees and trainers from Swagger, and then use these queries to inspect the stored data:

```sql
SELECT id, username, first_name, last_name, role, is_active
FROM users
ORDER BY id;

SELECT trainee.id, user.username, trainee.date_of_birth, trainee.address
FROM trainees trainee
JOIN users user ON user.id = trainee.user_id;

SELECT trainer.id, user.username,
       training_type.training_type_name AS specialization
FROM trainers trainer
JOIN users user ON user.id = trainer.user_id
JOIN training_types training_type
  ON training_type.id = trainer.specialization_id;

SELECT * FROM trainee2trainer;
SELECT * FROM trainings;
SELECT * FROM training_types;
```

The `password` column in `users` contains a BCrypt hash, never the generated plain-text password. Because the database is in memory and Hibernate uses `create-drop`, all local data is removed when the application stops.

## Authentication

Trainee and trainer registration are public. All other business endpoints require HTTP Basic authentication.

Registration returns a generated username and password once. The password contains 10 characters and the response uses `Cache-Control: no-store`. Only its BCrypt hash is stored in the database. Passwords and password hashes are not included in API models or logs.

An inactive user cannot use normal operations, but can provide their own credentials to reactivate their account. A user cannot change the status or profile of another account.

## Main endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/trainees` | Register a trainee and return credentials |
| `POST` | `/api/v1/trainers` | Register a trainer and return credentials |
| `GET` | `/api/v1/auth/login` | Validate credentials and active status |
| `PUT` | `/api/v1/auth/password` | Change the authenticated user's password |
| `GET` | `/api/v1/trainees/{username}` | Get a trainee profile |
| `PUT` | `/api/v1/trainees/{username}` | Update a trainee profile |
| `DELETE` | `/api/v1/trainees/{username}` | Delete a trainee and their trainings |
| `GET` | `/api/v1/trainers/{username}` | Get a trainer profile |
| `PUT` | `/api/v1/trainers/{username}` | Update a trainer profile |
| `GET` | `/api/v1/trainees/{username}/available-trainers` | List active, unassigned trainers |
| `PUT` | `/api/v1/trainees/{username}/trainers` | Replace the trainee's trainer list |
| `GET` | `/api/v1/trainees/{username}/trainings` | Get trainee trainings using optional filters |
| `GET` | `/api/v1/trainers/{username}/trainings` | Get trainer trainings using optional filters |
| `POST` | `/api/v1/trainings` | Create a training |
| `PATCH` | `/api/v1/users/{username}/status` | Activate or deactivate an account |
| `GET` | `/api/v1/training-types` | List the fixed training types |

The public filter parameters `trainerName` and `traineeName` represent profile usernames. A training type is automatically derived from the trainer specialization when a training is created.

## Trying the API

1. Open Swagger UI.
2. Run `POST /api/v1/trainees` or `POST /api/v1/trainers`.
3. Copy the generated username and password from the response.
4. Select **Authorize** and enter those credentials using HTTP Basic.
5. Run the protected endpoints directly from Swagger.

## Responses and errors

Successful registrations return `201 Created`. Other successful operations return `200 OK`.

Errors use the `application/problem+json` format:

- `400 Bad Request`: invalid request data
- `401 Unauthorized`: invalid credentials or inactive user
- `403 Forbidden`: attempt to operate on another account
- `404 Not Found`: resource does not exist
- `409 Conflict`: invalid or repeated status transition

Every response includes an `X-Transaction-Id` header. The same value is added to application logs to make requests easier to trace.

## Architecture and persistence

The project keeps a simple layered structure:

```text
REST Controllers -> Services -> Hibernate DAOs -> H2
                         |
                         +-> Authentication and business rules
```

- Controllers use request and response DTOs. Hibernate entities are not exposed by the API.
- Services contain transactions and business rules.
- DAOs contain Hibernate queries and persistence operations.
- `UserRole` ensures that one account is either a trainee or a trainer.
- Profile updates assign the requested state idempotently.
- The status endpoint returns `409 Conflict` when the requested transition has already happened.

The application automatically creates the fixed training types `FITNESS`, `YOGA`, `ZUMBA`, `STRETCHING` and `RESISTANCE` at startup.

Default database settings are located in `src/main/resources/application.properties`:

```properties
db.url=jdbc:h2:mem:gymcrm;DB_CLOSE_DELAY=-1
db.driver=org.h2.Driver
db.username=sa
db.password=
hibernate.hbm2ddl.auto=create-drop
```

## Tests and coverage

Run the complete verification process with:

```bash
mvn clean verify
```

This command:

- compiles and packages the application;
- runs the unit, persistence and REST integration tests;
- creates the JaCoCo coverage report;
- fails if line coverage is below 80%.

Run only the tests with:

```bash
mvn test
```

After `verify`, open the coverage report at:

```text
target/site/jacoco/index.html
```

The REST integration test exercises the complete task flow, including registration, authentication, profile updates, trainer assignment, training creation and filtering, password changes, account status changes and trainee deletion.
