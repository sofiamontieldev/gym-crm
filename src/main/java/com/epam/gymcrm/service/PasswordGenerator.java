package com.epam.gymcrm.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final int PASSWORD_LENGTH = 10;
    private static final char[] ALLOWED_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
                    .toCharArray();

    private final SecureRandom secureRandom;

    public PasswordGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public String generate() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int index = 0; index < PASSWORD_LENGTH; index++) {
            int characterIndex = secureRandom.nextInt(ALLOWED_CHARACTERS.length);
            password.append(ALLOWED_CHARACTERS[characterIndex]);
        }

        return password.toString();
    }
}
