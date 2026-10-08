package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private UserDAO userDAO;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        authenticationService = new AuthenticationService(userDAO);
    }

    @Test
    void authenticatesActiveUserWithValidCredentials() {
        User user = user(true);
        when(userDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(user));

        assertSame(user, authenticationService.authenticate("Valentina.Rojas", "Abc1234567"));
    }

    @Test
    void rejectsUnknownUsernameAndIncorrectPasswordWithoutExposingPassword() {
        when(userDAO.findByUsername("Persona.Inexistente")).thenReturn(Optional.empty());
        when(userDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(user(true)));

        AuthenticationException missingUser = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate("Persona.Inexistente", "Secret1234"));
        AuthenticationException wrongPassword = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate("Valentina.Rojas", "Wrong12345"));

        assertFalse(missingUser.getMessage().contains("Secret1234"));
        assertFalse(wrongPassword.getMessage().contains("Wrong12345"));
    }

    @Test
    void rejectsInactiveAuthenticationButAllowsCredentialVerification() {
        User user = user(false);
        when(userDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(user));

        assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate("Valentina.Rojas", "Abc1234567"));
        assertSame(
                user,
                authenticationService.verifyCredentials("Valentina.Rojas", "Abc1234567"));
    }

    private static User user(boolean active) {
        return new User.Builder()
                .setFirstName("Valentina")
                .setLastName("Rojas")
                .setUsername("Valentina.Rojas")
                .setPassword("Abc1234567")
                .setActive(active)
                .build();
    }
}
