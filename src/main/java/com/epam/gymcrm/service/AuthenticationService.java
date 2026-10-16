package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String INVALID_CREDENTIALS = "Invalid username or password";

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        User user = verifyCredentials(username, password);
        if (!user.isActive()) {
            log.warn("Authentication rejected for inactive username {}", username);
            throw new AuthenticationException("User is inactive");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User verifyCredentials(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            throw rejectedAuthentication(username);
        }

        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> rejectedAuthentication(username));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw rejectedAuthentication(username);
        }
        return user;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = verifyCredentials(username, currentPassword);
        if (newPassword == null || newPassword.length() != 10) {
            throw new IllegalArgumentException("newPassword must contain exactly 10 characters");
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for username {}", username);
    }

    private static AuthenticationException rejectedAuthentication(String username) {
        log.warn("Authentication rejected for username {}", username);
        return new AuthenticationException(INVALID_CREDENTIALS);
    }
}
