package com.epam.gymcrm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    @Test
    void shouldGenerateBaseUsernameForFirstUser() {
        UsernameGenerator generator = new UsernameGenerator();

        assertEquals("Pepito.Perez", generator.generate("Pepito", "Perez"));
    }

    @Test
    void shouldAddSerialSuffixForRepeatedNames() {
        UsernameGenerator generator = new UsernameGenerator();

        assertEquals("Pepito.Perez", generator.generate("Pepito", "Perez"));
        assertEquals("Pepito.Perez1", generator.generate("Pepito", "Perez"));
        assertEquals("Pepito.Perez2", generator.generate("Pepito", "Perez"));
    }
}
