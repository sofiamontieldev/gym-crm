package com.epam.gymcrm.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RestApiIntegrationTest {

    private final int port;

    @Autowired
    RestApiIntegrationTest(@LocalServerPort int port) {
        this.port = port;
    }

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
    }

    @Test
    void executesTheCompleteRestTaskContract() {
        Response traineeRegistration = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Elena",
                        "lastName", "Ramirez",
                        "dateOfBirth", "1996-03-04",
                        "address", "Bogota"))
                .when().post("/api/v1/trainees")
                .then().statusCode(201)
                .header("Cache-Control", containsString("no-store"))
                .body("username", not(nullValue()))
                .body("password.length()", equalTo(10))
                .extract().response();
        String trainee = traineeRegistration.path("username");
        String traineePassword = traineeRegistration.path("password");

        Response trainerRegistration = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Mateo",
                        "lastName", "Salazar",
                        "specialization", "YOGA"))
                .when().post("/api/v1/trainers")
                .then().statusCode(201)
                .body("username", not(nullValue()))
                .body("password.length()", equalTo(10))
                .extract().response();
        String trainer = trainerRegistration.path("username");
        String trainerPassword = trainerRegistration.path("password");

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/actuator/health")
                .then().statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.gymDatabase.status", equalTo("UP"))
                .body("components.trainingCatalog.status", equalTo("UP"));

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/actuator/prometheus")
                .then().statusCode(200)
                .body(containsString("gym_profile_registrations_total"));

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/api/v1/auth/login")
                .then().statusCode(200);

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/api/v1/trainees/{username}", trainee)
                .then().statusCode(200)
                .body("firstName", equalTo("Elena"))
                .body("lastName", equalTo("Ramirez"))
                .body("dateOfBirth", equalTo("1996-03-04"))
                .body("address", equalTo("Bogota"))
                .body("active", equalTo(true))
                .body("trainers", hasSize(0));

        given().auth().preemptive().basic(trainee, traineePassword)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Elena",
                        "lastName", "Mendoza",
                        "dateOfBirth", "1996-03-04",
                        "address", "Medellin",
                        "active", true))
                .when().put("/api/v1/trainees/{username}", trainee)
                .then().statusCode(200)
                .body("username", equalTo(trainee))
                .body("firstName", equalTo("Elena"))
                .body("lastName", equalTo("Mendoza"))
                .body("dateOfBirth", equalTo("1996-03-04"))
                .body("address", equalTo("Medellin"))
                .body("active", equalTo(true))
                .body("trainers", hasSize(0));

        given().auth().preemptive().basic(trainer, trainerPassword)
                .when().get("/api/v1/trainers/{username}", trainer)
                .then().statusCode(200)
                .body("firstName", equalTo("Mateo"))
                .body("lastName", equalTo("Salazar"))
                .body("specialization", equalTo("YOGA"))
                .body("active", equalTo(true))
                .body("trainees", hasSize(0));

        given().auth().preemptive().basic(trainer, trainerPassword)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "Mateo", "lastName", "Restrepo", "active", true))
                .when().put("/api/v1/trainers/{username}", trainer)
                .then().statusCode(200)
                .body("username", equalTo(trainer))
                .body("firstName", equalTo("Mateo"))
                .body("lastName", equalTo("Restrepo"))
                .body("specialization", equalTo("YOGA"))
                .body("active", equalTo(true))
                .body("trainees", hasSize(0));

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/api/v1/trainees/{username}/available-trainers", trainee)
                .then().statusCode(200)
                .body("username[0]", equalTo(trainer))
                .body("firstName[0]", equalTo("Mateo"))
                .body("lastName[0]", equalTo("Restrepo"))
                .body("specialization[0]", equalTo("YOGA"));

        given().auth().preemptive().basic(trainee, traineePassword)
                .contentType(ContentType.JSON)
                .body(Map.of("trainerUsernames", List.of(trainer)))
                .when().put("/api/v1/trainees/{username}/trainers", trainee)
                .then().statusCode(200)
                .body("username[0]", equalTo(trainer))
                .body("firstName[0]", equalTo("Mateo"))
                .body("lastName[0]", equalTo("Restrepo"))
                .body("specialization[0]", equalTo("YOGA"));

        given().auth().preemptive().basic(trainer, trainerPassword)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "traineeUsername", trainee,
                        "trainerUsername", trainer,
                        "trainingName", "Morning Yoga",
                        "trainingDate", "2026-10-15",
                        "trainingDuration", 60))
                .when().post("/api/v1/trainings")
                .then().statusCode(200).body("trainingType", equalTo("YOGA"));

        given().auth().preemptive().basic(trainee, traineePassword)
                .queryParam("fromDate", "2026-10-01")
                .queryParam("toDate", "2026-10-31")
                .queryParam("trainerName", trainer)
                .queryParam("trainingType", "YOGA")
                .when().get("/api/v1/trainees/{username}/trainings", trainee)
                .then().statusCode(200)
                .body("trainingName[0]", equalTo("Morning Yoga"))
                .body("trainingDate[0]", equalTo("2026-10-15"))
                .body("trainingType[0]", equalTo("YOGA"))
                .body("trainingDuration[0]", equalTo(60))
                .body("trainerName[0]", equalTo(trainer));

        given().auth().preemptive().basic(trainer, trainerPassword)
                .queryParam("traineeName", trainee)
                .when().get("/api/v1/trainers/{username}/trainings", trainer)
                .then().statusCode(200)
                .body("trainingName[0]", equalTo("Morning Yoga"))
                .body("trainingDate[0]", equalTo("2026-10-15"))
                .body("trainingType[0]", equalTo("YOGA"))
                .body("trainingDuration[0]", equalTo(60))
                .body("traineeName[0]", equalTo(trainee));

        given().auth().preemptive().basic(trainee, traineePassword)
                .when().get("/api/v1/training-types")
                .then().statusCode(200)
                .body("id", hasSize(5))
                .body("trainingTypeName", hasSize(5));

        String changedPassword = "NewPass123";
        given().auth().preemptive().basic(trainee, traineePassword)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", trainee,
                        "oldPassword", traineePassword,
                        "newPassword", changedPassword))
                .when().put("/api/v1/auth/password")
                .then().statusCode(200);

        given().auth().preemptive().basic(trainee, changedPassword)
                .contentType(ContentType.JSON).body(Map.of("active", false))
                .when().patch("/api/v1/users/{username}/status", trainee)
                .then().statusCode(200);

        given().auth().preemptive().basic(trainee, changedPassword)
                .when().get("/api/v1/trainees/{username}", trainee)
                .then().statusCode(401);

        given().auth().preemptive().basic(trainee, changedPassword)
                .contentType(ContentType.JSON).body(Map.of("active", false))
                .when().patch("/api/v1/users/{username}/status", trainee)
                .then().statusCode(409);

        given().auth().preemptive().basic(trainee, changedPassword)
                .contentType(ContentType.JSON).body(Map.of("active", true))
                .when().patch("/api/v1/users/{username}/status", trainee)
                .then().statusCode(200);

        given().auth().preemptive().basic(trainee, changedPassword)
                .when().delete("/api/v1/trainees/{username}", trainee)
                .then().statusCode(200);
    }

    @Test
    void exposesSwaggerAndConsistentClientErrors() {
        Credentials credentials = registerTrainee("Sofia", "Herrera");

        given().auth().preemptive().basic(credentials.username(), credentials.password())
                .when().get("/v3/api-docs")
                .then().statusCode(200)
                .body("paths.'/api/v1/trainees'.post", not(nullValue()))
                .body("components.schemas.TraineeUpdateResponse.properties.username",
                        not(nullValue()))
                .body("components.schemas.TrainerUpdateResponse.properties.username",
                        not(nullValue()));

        given().contentType(ContentType.JSON)
                .body(Map.of("firstName", "", "lastName", "Herrera"))
                .when().post("/api/v1/trainees")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("transactionId", not(nullValue()));

        given().auth().preemptive().basic("unknown", "bad-password")
                .when().get("/api/v1/auth/login")
                .then().statusCode(401)
                .contentType("application/problem+json");
    }

    @Test
    void requiresAuthenticationForEveryOperationExceptRegistration() {
        given().when().get("/api/v1/training-types")
                .then().statusCode(401);
        given().when().get("/actuator/health")
                .then().statusCode(200)
                .body("status", equalTo("UP"));
        given().when().get("/actuator/prometheus")
                .then().statusCode(401);
        given().when().get("/v3/api-docs")
                .then().statusCode(401);
        given().when().get("/h2-console/")
                .then().statusCode(401);
    }

    @Test
    void preventsAuthenticatedUserFromOperatingOnAnotherProfile() {
        Credentials first = registerTrainee("Laura", "Gomez");
        Credentials second = registerTrainee("Daniel", "Torres");

        given().auth().preemptive().basic(first.username(), first.password())
                .when().get("/api/v1/trainees/{username}", second.username())
                .then().statusCode(403)
                .contentType("application/problem+json");
    }

    private static Credentials registerTrainee(String firstName, String lastName) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "dateOfBirth", "1996-03-04",
                        "address", "Bogota"))
                .when().post("/api/v1/trainees")
                .then().statusCode(201)
                .extract().response();

        return new Credentials(response.path("username"), response.path("password"));
    }

    private record Credentials(String username, String password) {
    }
}
