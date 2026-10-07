package com.epam.gymcrm.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void shouldUpdatePersonalData() {
        User user = createUser();

        user.updatePersonalData("Jose", "Vargas");

        assertEquals("Jose", user.getFirstName());
        assertEquals("Vargas", user.getLastName());
    }

    @Test
    void shouldActivateAndDeactivateUser() {
        User user = createUser();

        user.deactivate();
        assertFalse(user.isActive());

        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    void shouldNotExposePasswordInToString() {
        User user = createUser();

        assertFalse(user.toString().contains(user.getPassword()));
    }

    private static User createUser() {
        return new User.Builder()
                .setId(1L)
                .setFirstName("Pepito")
                .setLastName("Perez")
                .setUsername("Pepito.Perez")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
    }
}
