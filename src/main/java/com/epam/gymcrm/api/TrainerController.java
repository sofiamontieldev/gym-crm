package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.RegistrationResult;
import com.epam.gymcrm.service.TrainerService;
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
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final ApiMapper mapper;
    private final ApiAccessService access;

    public TrainerController(
            TrainerService trainerService,
            TrainingService trainingService,
            ApiMapper mapper,
            ApiAccessService access) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.mapper = mapper;
        this.access = access;
    }

    @PostMapping
    @Operation(summary = "Register a trainer")
    public ResponseEntity<ApiDtos.CredentialsResponse> register(
            @Valid @RequestBody ApiDtos.TrainerRegistrationRequest request) {
        RegistrationResult<?> result = trainerService.registerTrainer(
                request.firstName(), request.lastName(), request.specialization());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(new ApiDtos.CredentialsResponse(result.username(), result.password()));
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Get trainer profile")
    public ApiDtos.TrainerProfileResponse get(Authentication authentication,
                                              @PathVariable String username) {
        access.requireActiveSelf(authentication, username);
        return mapper.toTrainerProfile(trainerService.selectTrainer(username));
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Update trainer profile; specialization is read-only")
    @ApiResponse(responseCode = "200", description = "Trainer profile updated successfully")
    public ApiDtos.TrainerUpdateResponse update(
            Authentication authentication,
            @PathVariable String username,
            @Valid @RequestBody ApiDtos.TrainerUpdateRequest request) {
        access.requireActiveSelf(authentication, username);
        return mapper.toTrainerUpdate(trainerService.updateTrainer(
                username, request.firstName(), request.lastName(), request.active()));
    }

    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Get trainer trainings with optional filters")
    public List<ApiDtos.TrainingResponse> trainings(
            Authentication authentication,
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @Parameter(description = "Username of the trainee profile")
            @RequestParam(required = false) String traineeName) {
        access.requireActiveSelf(authentication, username);
        return trainingService.selectTrainerTrainings(
                        username, fromDate, toDate, traineeName).stream()
                .map(mapper::toTraining)
                .toList();
    }
}
