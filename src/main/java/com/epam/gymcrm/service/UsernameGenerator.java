package com.epam.gymcrm.service;

import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {

    private final Set<String> assignedUsernames = new HashSet<>();

    public synchronized String generate(String firstName, String lastName) {
        return generateUniqueUsername(firstName, lastName, assignedUsernames);
    }

    public synchronized String generate(
            String firstName,
            String lastName,
            Collection<? extends User> existingUsers) {

        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.addAll(assignedUsernames);

        for (User user : existingUsers) {
            existingUsernames.add(user.getUsername());
        }

        String username = generateUniqueUsername(firstName, lastName, existingUsernames);
        assignedUsernames.add(username);
        return username;
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
