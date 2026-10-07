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

        user.updatePersonalData("Michael", "Brown");

        assertEquals("Michael", user.getFirstName());
        assertEquals("Brown", user.getLastName());
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
                .setFirstName("John")
                .setLastName("Smith")
                .setUsername("John.Smith")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
    }
}
