# Gym CRM

Spring Core and Hibernate module for managing trainees, trainers and trainings with an H2 database.

## Requirements

- JDK 21
- Maven
- IntelliJ IDEA or another Java IDE

Verify the tools with:

```bash
java -version
mvn -version
```

## Quick start with IntelliJ IDEA

1. Extract the `.rar` or `.zip` archive.
2. Open the extracted folder in IntelliJ IDEA using `pom.xml`.
3. Select JDK 21 as the project SDK and Maven JDK.
4. Open a terminal in the project root and run:

```bash
mvn clean test
```

This command compiles the project, creates the temporary H2 schema and runs the complete test suite.

Run the application with:

```bash
mvn exec:java
```

## Implemented functionality

- Trainee and trainer registration with generated credentials.
- Authentication and profile lookup by username.
- Profile updates, password changes, activation and deactivation.
- Transactional hard deletion of trainees while preserving trainers.
- Training creation and filtered training searches.
- Active unassigned trainer lookup and trainee-trainer assignment updates.
- Automatic, idempotent initialization of the five fixed training types.

All operations except registration require valid credentials. Passwords contain exactly 10 characters and are excluded from logs and `toString()` output. They remain plain text only for this academic stage; a production system should use a secure password-hashing algorithm such as Argon2id, bcrypt or scrypt.

## Architecture and persistence

```text
GymCrmFacade
      |
      +--> TraineeService --+
      +--> TrainerService --+--> Hibernate DAOs --> SessionFactory --> H2
      +--> TrainingService -+
             |
       AuthenticationService
```

Dependencies use constructor injection. Write operations are transactional, while read operations use read-only transactions where appropriate. Hibernate is the only source of application data.

The persistent model uses composition:

- `Trainee` has one `User`.
- `Trainer` has one `User` and one `TrainingType` specialization.
- `Training` references a trainee, trainer and training type.
- `Trainee2Trainer` stores the many-to-many assignments.

The default settings are in `src/main/resources/application.properties`:

```properties
db.url=jdbc:h2:mem:gymcrm;DB_CLOSE_DELAY=-1
db.driver=org.h2.Driver
db.username=sa
db.password=
hibernate.hbm2ddl.auto=create-drop
```

No external database installation is required. The in-memory database is discarded when the application context closes. At startup, the application inserts `FITNESS`, `YOGA`, `ZUMBA`, `STRETCHING` and `RESISTANCE`; it does not insert demo profiles or trainings.

## Optional H2 inspection

The automated tests are the main verification mechanism. To keep the database after a test and inspect its tables manually, temporarily change these two properties:

```properties
db.url=jdbc:h2:file:./data/gymcrm
hibernate.hbm2ddl.auto=create
```

Run one integration scenario, for example:

```powershell
mvn "-Dtest=ServiceIntegrationTest#generatesPersistentUsernamesAndReturnsInitializedTraineeProfile" test
```

Then start the H2 web console:

```powershell
mvn exec:java "-Dexec.mainClass=org.h2.tools.Console" "-Dexec.classpathScope=runtime"
```

Use these connection settings:

- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:file:./data/gymcrm`
- User: `sa`
- Password: leave empty

Useful queries:

```sql
SELECT * FROM USERS;
SELECT * FROM TRAINEES;
SELECT * FROM TRAINERS;
SELECT * FROM TRAININGS;
SELECT * FROM TRAINING_TYPES;
SELECT * FROM TRAINEE2TRAINER;
```

Stop the console and restore the original in-memory URL and `create-drop` setting after the inspection. Files created under `data/` are ignored by Git.

## Tests

The suite uses JUnit 5 and Mockito for focused unit tests, plus Spring and H2 integration tests for mappings, DAO queries, initialization, transactions, cascades and trainer preservation. It also verifies that passwords are absent from logs and `toString()` output.

Run all tests with:

```bash
mvn clean test
```
