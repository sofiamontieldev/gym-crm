package com.epam.gymcrm.model;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

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

        Logger logger = (Logger) LoggerFactory.getLogger(User.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(IllegalStateException.class, user::activate);
            user.deactivate();
            assertFalse(user.isActive());
            assertThrows(IllegalStateException.class, user::deactivate);
        } finally {
            logger.detachAppender(appender);
        }

        user.activate();
        assertTrue(user.isActive());
        assertEquals(2, appender.list.size());
        assertTrue(appender.list.stream()
                .allMatch(event -> event.getLevel() == Level.WARN));
        assertTrue(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .allMatch(message -> message.contains("Juan.Perez")));
    }

    @Test
    void shouldChangePasswordOnlyWhenItHasTenCharacters() {
        User user = createUser();

        user.changePassword("NewPass123");

        assertEquals("NewPass123", user.getPassword());
        assertThrows(IllegalArgumentException.class, () -> user.changePassword("short"));
    }

    @Test
    void shouldNotExposePasswordInToString() {
        User user = createUser();

        assertFalse(user.toString().contains(user.getPassword()));
    }

    private static User createUser() {
        return new User.Builder()
                .setId(1L)
                .setFirstName("Juan")
                .setLastName("Perez")
                .setUsername("Juan.Perez")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
    }
}
