package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.security.ApiAccessService;
import com.epam.gymcrm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@SecurityRequirement(name = "basicAuth")
public class UserController {

    private final UserService userService;
    private final ApiAccessService access;

    public UserController(UserService userService, ApiAccessService access) {
        this.userService = userService;
        this.access = access;
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate own account; repeated transition returns 409")
    public ApiDtos.MessageResponse changeStatus(
            Authentication authentication,
            @PathVariable String username,
            @Valid @RequestBody ApiDtos.StatusUpdateRequest request) {
        access.requireSelf(authentication, username);
        userService.changeStatus(username, request.active());
        return new ApiDtos.MessageResponse(request.active() ? "User activated" : "User deactivated");
    }
}
