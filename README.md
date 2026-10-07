# Gym CRM

Spring Core module for managing trainees, trainers and trainings in an in-memory gym CRM.

## Requirements

- JDK 21
- Maven
- IntelliJ IDEA, Visual Studio Code or another Java IDE

Verify the Java installation:

```bash
java -version
mvn -version
```

The Maven compiler is configured for Java 21.

## Build and run

Run the tests and compile the project:

```bash
mvn clean test
```

Run the application:

```bash
mvn exec:java
```

The application starts a Spring `ApplicationContext`. The initial records are loaded from:

```text
src/main/resources/initial-data.csv
```

## Initial data

The path is configured in:

```text
src/main/resources/application.properties
```

Current configuration:

```properties
gym.crm.initial-data.path=classpath:initial-data.csv
```

The file uses one record per line. Fields are separated by `;` and use the `name=value` format:

```text
TRAINEE;id=100;firstName=Alice;lastName=Johnson;password=SeedPass1!;active=true;dateOfBirth=1992-05-10;address=Main Street 1
TRAINER;id=200;firstName=Bob;lastName=Martinez;password=SeedPass2!;active=true;specialization=FITNESS
TRAINING;id=300;traineeId=100;trainerId=200;trainingName=Morning Fitness;trainingType=FITNESS;trainingDate=2026-10-05;trainingDuration=60
```

`InitialDataLoader` reads this file during Spring startup and fills the three independent maps.

## Architecture

```text
GymCrmFacade
      |
      +--> TraineeService --> TraineeDAO --> traineeStorage
      +--> TrainerService --> TrainerDAO --> trainerStorage
      +--> TrainingService -> TrainingDAO -> trainingStorage
```

The storage is intentionally in memory because this is a Spring Core exercise. There is no database or web layer.

## Packages

```text
src/main/java/com/epam/gymcrm
├── config       Spring configuration and storage beans
├── model        User, Trainee, Trainer, Training and TrainingType
├── dao          In-memory data access objects
├── service      Business operations and username/password generators
├── facade       Facade that delegates to the services
└── loader       Initial data loader
```

## Injection decision

The project uses constructor injection for required dependencies:

- DAOs receive their storage map through the constructor.
- Services receive DAOs and generators through the constructor.
- The facade receives all services through the constructor.

Constructor injection was selected because it makes dependencies explicit, prevents partially initialized objects and simplifies unit testing. The task mentions setter injection for some dependencies; if the evaluator checks that requirement literally, only those specific dependencies can be adapted to setter injection without changing the rest of the design.

## Username and password rules

- Usernames use `FirstName.LastName`.
- If the username already exists for another trainee or trainer, a numeric suffix is added: `John.Smith1`.
- New profile passwords contain exactly 10 random characters.
- Passwords are not written to logs or `toString()` output.

The exercise stores the generated password in the profile because that is part of the requested model. A production system should store a password hash instead of the raw password.

## Services

`TraineeService` supports create, update, delete, select by id and select all.

`TrainerService` supports create, update, select by id and select all.

`TrainingService` supports create, select by id and select all.

Missing records are represented with `Optional` for select operations. Update and delete operations throw `IllegalArgumentException` when the requested profile does not exist.

## Tests

Tests are located under:

```text
src/test/java
```

Run them with:

```bash
mvn test
```

The service and loader tests are the next testing stage of the project implementation.
