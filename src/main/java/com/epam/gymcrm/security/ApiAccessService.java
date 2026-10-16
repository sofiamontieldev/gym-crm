package com.epam.gymcrm.security;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessService {

    public GymPrincipal requireActive(Authentication authentication) {
        GymPrincipal principal = principal(authentication);
        if (!principal.active()) {
            throw new AuthenticationException("User is inactive");
        }
        return principal;
    }

    public GymPrincipal requireActiveSelf(Authentication authentication, String username) {
        GymPrincipal principal = requireSelf(authentication, username);
        if (!principal.active()) {
            throw new AuthenticationException("User is inactive");
        }
        return principal;
    }

    public GymPrincipal requireSelf(Authentication authentication, String username) {
        GymPrincipal principal = principal(authentication);
        if (!principal.username().equals(username)) {
            throw new ForbiddenException("A user can only operate on their own account");
        }
        return principal;
    }

    private GymPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof GymPrincipal principal)) {
            throw new AuthenticationException("Authentication is required");
        }
        return principal;
    }
}
