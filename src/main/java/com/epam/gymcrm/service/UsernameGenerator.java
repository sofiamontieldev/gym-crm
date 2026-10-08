package com.epam.gymcrm.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {

    private final Set<String> assignedUsernames = new HashSet<>();

    public synchronized String generate(String firstName, String lastName) {
        return generateUniqueUsername(firstName, lastName, assignedUsernames);
    }

    private String generateUniqueUsername(
            String firstName,
            String lastName,
            Set<String> existingUsernames) {

        String baseUsername = firstName + "." + lastName;
        if (!existingUsernames.contains(baseUsername)) {
            assignedUsernames.add(baseUsername);
            return baseUsername;
        }

        int suffix = 1;
        String candidate;

        do {
            candidate = baseUsername + suffix;
            suffix++;
        } while (existingUsernames.contains(candidate));

        assignedUsernames.add(candidate);
        return candidate;
    }
}
