package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@Tag(name = "Training Types")
@SecurityRequirement(name = "basicAuth")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final ApiMapper mapper;
    private final ApiAccessService access;

    public TrainingTypeController(
            TrainingTypeService trainingTypeService,
            ApiMapper mapper,
            ApiAccessService access) {
        this.trainingTypeService = trainingTypeService;
        this.mapper = mapper;
        this.access = access;
    }

    @GetMapping
    @Operation(summary = "List fixed training types")
    public List<ApiDtos.TrainingTypeResponse> findAll(Authentication authentication) {
        access.requireActive(authentication);
        return trainingTypeService.findAll().stream().map(mapper::toTrainingType).toList();
    }
}
