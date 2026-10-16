package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.RegistrationResult;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@Tag(name = "Trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final ApiMapper mapper;
    private final ApiAccessService access;

    public TraineeController(
            TraineeService traineeService,
            TrainingService trainingService,
            ApiMapper mapper,
            ApiAccessService access) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.mapper = mapper;
        this.access = access;
    }

    @PostMapping
    @Operation(summary = "Register a trainee")
    public ResponseEntity<ApiDtos.CredentialsResponse> register(
            @Valid @RequestBody ApiDtos.TraineeRegistrationRequest request) {

        RegistrationResult<?> result = traineeService.registerTrainee(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.address());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(new ApiDtos.CredentialsResponse(result.username(), result.password()));
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Get trainee profile")
    public ApiDtos.TraineeProfileResponse get(Authentication authentication,
                                              @PathVariable String username) {
        access.requireActiveSelf(authentication, username);
        return mapper.toTraineeProfile(traineeService.selectTrainee(username));
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Update trainee profile; status assignment is idempotent")
    @ApiResponse(responseCode = "200", description = "Trainee profile updated successfully")
    public ApiDtos.TraineeUpdateResponse update(
            Authentication authentication,
            @PathVariable String username,
            @Valid @RequestBody ApiDtos.TraineeUpdateRequest request) {

        access.requireActiveSelf(authentication, username);
        return mapper.toTraineeUpdate(traineeService.updateTrainee(
                username, request.firstName(), request.lastName(), request.dateOfBirth(),
                request.address(), request.active()));
    }

    @DeleteMapping("/{username}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Delete trainee and their trainings")
    public ApiDtos.MessageResponse delete(Authentication authentication,
                                          @PathVariable String username) {
        access.requireActiveSelf(authentication, username);
        traineeService.deleteTrainee(username);
        return new ApiDtos.MessageResponse("Trainee deleted");
    }

    @GetMapping("/{username}/available-trainers")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "List active trainers not assigned to the trainee")
    public List<ApiDtos.TrainerSummary> availableTrainers(
            Authentication authentication,
            @PathVariable String username) {
        access.requireActiveSelf(authentication, username);
        return traineeService.selectActiveUnassignedTrainers(username).stream()
                .map(mapper::toTrainerSummary)
                .toList();
    }

    @PutMapping("/{username}/trainers")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Replace the trainee's trainer list")
    public List<ApiDtos.TrainerSummary> replaceTrainers(
            Authentication authentication,
            @PathVariable String username,
            @Valid @RequestBody ApiDtos.TrainerAssignmentsRequest request) {
        access.requireActiveSelf(authentication, username);
        traineeService.replaceTrainers(username, request.trainerUsernames());
        return traineeService.selectTrainee(username).getTrainers().stream()
                .map(mapper::toTrainerSummary)
                .toList();
    }

    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Get trainee trainings with optional filters")
    public List<ApiDtos.TrainingResponse> trainings(
            Authentication authentication,
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @Parameter(description = "Username of the trainer profile")
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        access.requireActiveSelf(authentication, username);
        return trainingService.selectTraineeTrainings(
                        username, fromDate, toDate, trainerName, trainingType).stream()
                .map(mapper::toTraining)
                .toList();
    }
}
