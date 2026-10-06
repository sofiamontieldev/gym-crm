package com.epam.gymcrm.service;

import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {

    public String generate(
            String firstName,
            String lastName,
            Collection<? extends User> existingUsers) {

        String baseUsername = firstName + "." + lastName;
        Set<String> existingUsernames = new HashSet<>();

        for (User user : existingUsers) {
            existingUsernames.add(user.getUsername());
        }

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        String candidate;

        do {
            candidate = baseUsername + suffix;
            suffix++;
        } while (existingUsernames.contains(candidate));

        return candidate;
    }
}
