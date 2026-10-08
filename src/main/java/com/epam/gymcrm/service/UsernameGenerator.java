package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDAO;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {

    private final UserDAO userDAO;
    private final Set<String> reservedUsernames = new HashSet<>();

    public UsernameGenerator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public synchronized String generate(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String candidate = baseUsername;
        int suffix = 1;
        while (userDAO.existsByUsername(candidate) || reservedUsernames.contains(candidate)) {
            candidate = baseUsername + suffix++;
        }

        reservedUsernames.add(candidate);
        return candidate;
    }
}
