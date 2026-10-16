package com.epam.gymcrm.service;

public record RegistrationResult<T>(T profile, String username, String password) {
}
