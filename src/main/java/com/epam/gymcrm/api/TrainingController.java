package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainings")
@Tag(name = "Trainings")
@SecurityRequirement(name = "basicAuth")
public class TrainingController {

    private final TrainingService trainingService;
    private final ApiMapper mapper;
    private final ApiAccessService access;

    public TrainingController(
            TrainingService trainingService,
            ApiMapper mapper,
            ApiAccessService access) {
        this.trainingService = trainingService;
        this.mapper = mapper;
        this.access = access;
    }

    @PostMapping
    @Operation(summary = "Create a training; type is derived from trainer specialization")
    public ApiDtos.TrainingResponse create(
            Authentication authentication,
            @Valid @RequestBody ApiDtos.TrainingCreateRequest request) {
        access.requireActive(authentication);
        return mapper.toTraining(trainingService.createTraining(
                request.traineeUsername(), request.trainerUsername(), request.trainingName(),
                request.trainingDate(), request.trainingDuration()));
    }
}
