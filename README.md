# Gym CRM

Spring Core and Hibernate module for managing trainees, trainers and trainings in an H2 in-memory database.

## Quick start with IntelliJ IDEA

1. Extract the `.rar` or `.zip` archive.
2. Open the extracted folder in IntelliJ IDEA using `pom.xml`.
3. Configure the project SDK as **JDK 21**.
4. Open a terminal in the project root and run:

```bash
mvn clean test
```

The command compiles the project, creates a temporary H2 schema and runs all tests. Maven must be installed and available in the system `PATH`.

## Requirements

- JDK 21
- Maven
- IntelliJ IDEA or another Java IDE

Verify the installation with:

```bash
java -version
mvn -version
```

## Run the application

```bash
mvn exec:java
```

The application starts a Spring `ApplicationContext`, creates the H2 database in memory and lets Hibernate generate the schema. No external database installation is required.

## Persistence

The JDBC and Hibernate settings are in `src/main/resources/application.properties`.

```properties
db.url=jdbc:h2:mem:gymcrm;DB_CLOSE_DELAY=-1
hibernate.hbm2ddl.auto=create-drop
```

The database exists while the application context is active and is discarded when the process ends. Hibernate is the only source of application data; the previous CSV and map storage are not used.

## Architecture

```text
GymCrmFacade
      |
      +--> TraineeService --> TraineeDAO --+
      +--> TrainerService --> TrainerDAO ---+--> Hibernate SessionFactory --> H2
      +--> TrainingService -> TrainingDAO --+
```

Required dependencies use constructor injection. Service write operations are transactional, and read operations use read-only transactions where appropriate.

The persistent model uses composition instead of inheritance:

- `Trainee` has one `User`.
- `Trainer` has one `User` and one `TrainingType` specialization.
- `Training` references a trainee, trainer and training type through foreign keys.
- Trainees and trainers use the `Trainee2Trainer` many-to-many join table.

## Business rules in this stage

- Usernames use the `FirstName.LastName` format and repeated names receive a numeric suffix.
- New passwords contain exactly 10 random characters.
- Passwords are excluded from logs and `toString()` output.
- Passwords remain plain text only because this is an academic Hibernate stage. A production system should store passwords with a secure password-hashing algorithm such as Argon2id, bcrypt or scrypt.
- Training duration must be greater than zero.
- Training types are persistent fixed values: `FITNESS`, `YOGA`, `ZUMBA`, `STRETCHING` and `RESISTANCE`.

Automatic seeding, authentication, training filters and complete transactional deletion are planned for the next implementation phases.

## Tests

Tests are located in `src/test/java` and cover generators, model behavior, service delegation, Spring/Hibernate configuration, schema creation and entity relationships using H2.

Run them with:

```bash
mvn clean test
```
