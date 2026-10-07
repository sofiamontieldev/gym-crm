package com.epam.gymcrm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    @Test
    void shouldGenerateBaseUsernameForFirstUser() {
        UsernameGenerator generator = new UsernameGenerator();

        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }

    @Test
    void shouldAddSerialSuffixForRepeatedNames() {
        UsernameGenerator generator = new UsernameGenerator();

        assertEquals("John.Smith", generator.generate("John", "Smith"));
        assertEquals("John.Smith1", generator.generate("John", "Smith"));
        assertEquals("John.Smith2", generator.generate("John", "Smith"));
    }
}
