package com.epam.gymcrm.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record TraineeRegistrationRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            LocalDate dateOfBirth,
            String address) {
    }

    public record TrainerRegistrationRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Schema(example = "FITNESS") String specialization) {
    }

    public record CredentialsResponse(String username, String password) {
    }

    public record PasswordChangeRequest(
            @NotBlank String username,
            @NotBlank String oldPassword,
            @NotBlank @Size(min = 10, max = 10) String newPassword) {
    }

    public record TraineeUpdateRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            LocalDate dateOfBirth,
            String address,
            boolean active) {
    }

    public record TrainerUpdateRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            boolean active) {
    }

    public record StatusUpdateRequest(@NotNull Boolean active) {
    }

    public record TrainerAssignmentsRequest(
            @NotNull List<@NotBlank String> trainerUsernames) {
    }

    public record TrainingCreateRequest(
            @NotBlank String traineeUsername,
            @NotBlank String trainerUsername,
            @NotBlank String trainingName,
            @NotNull LocalDate trainingDate,
            @Positive int trainingDuration) {
    }

    public record TrainerSummary(
            String username,
            String firstName,
            String lastName,
            String specialization) {
    }

    public record TraineeSummary(String username, String firstName, String lastName) {
    }

    public record TraineeProfileResponse(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            boolean active,
            List<TrainerSummary> trainers) {
    }

    public record TraineeUpdateResponse(
            String username,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            boolean active,
            List<TrainerSummary> trainers) {
    }

    public record TrainerProfileResponse(
            String firstName,
            String lastName,
            String specialization,
            boolean active,
            List<TraineeSummary> trainees) {
    }

    public record TrainerUpdateResponse(
            String username,
            String firstName,
            String lastName,
            String specialization,
            boolean active,
            List<TraineeSummary> trainees) {
    }

    public record TrainingResponse(
            String trainingName,
            LocalDate trainingDate,
            String trainingType,
            int trainingDuration,
            @Schema(description = "Username of the trainee profile") String traineeName,
            @Schema(description = "Username of the trainer profile") String trainerName) {
    }

    public record TrainingTypeResponse(Long id, String trainingTypeName) {
    }

    public record MessageResponse(String message) {
    }
}
