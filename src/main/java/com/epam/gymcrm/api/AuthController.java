package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
@SecurityRequirement(name = "basicAuth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ApiAccessService access;

    public AuthController(AuthenticationService authenticationService, ApiAccessService access) {
        this.authenticationService = authenticationService;
        this.access = access;
    }

    @GetMapping("/login")
    @Operation(summary = "Validate active user credentials")
    public ApiDtos.MessageResponse login(Authentication authentication) {
        access.requireActive(authentication);
        return new ApiDtos.MessageResponse("Authenticated");
    }

    @PutMapping("/password")
    @Operation(summary = "Change the authenticated user's password")
    public ApiDtos.MessageResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody ApiDtos.PasswordChangeRequest request) {

        access.requireActiveSelf(authentication, request.username());
        authenticationService.changePassword(
                request.username(), request.oldPassword(), request.newPassword());
        return new ApiDtos.MessageResponse("Password changed");
    }
}
