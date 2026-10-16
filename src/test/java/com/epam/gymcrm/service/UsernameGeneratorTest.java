package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDAO;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsernameGeneratorTest {

    @Test
    void generatesBaseUsernameWhenItDoesNotExist() {
        UserDAO userDAO = mock(UserDAO.class);
        when(userDAO.existsByUsername("Valentina.Rojas")).thenReturn(false);
        UsernameGenerator generator = new UsernameGenerator(userDAO);

        assertEquals("Valentina.Rojas", generator.generate("Valentina", "Rojas"));
    }

    @Test
    void addsFirstAvailableSerialSuffixUsingPersistedUsernames() {
        UserDAO userDAO = mock(UserDAO.class);
        when(userDAO.existsByUsername("Valentina.Rojas")).thenReturn(true);
        when(userDAO.existsByUsername("Valentina.Rojas1")).thenReturn(true);
        when(userDAO.existsByUsername("Valentina.Rojas2")).thenReturn(false);
        UsernameGenerator generator = new UsernameGenerator(userDAO);

        assertEquals("Valentina.Rojas2", generator.generate("Valentina", "Rojas"));
    }

    @Test
    void reservesDifferentUsernamesForConcurrentRegistrations() throws Exception {
        UserDAO userDAO = mock(UserDAO.class);
        UsernameGenerator generator = new UsernameGenerator(userDAO);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<String> firstResult = executor.submit(() -> {
                start.await();
                return generator.generate("Valentina", "Rojas");
            });
            Future<String> secondResult = executor.submit(() -> {
                start.await();
                return generator.generate("Valentina", "Rojas");
            });

            start.countDown();
            List<String> usernames = List.of(
                    firstResult.get(2, TimeUnit.SECONDS),
                    secondResult.get(2, TimeUnit.SECONDS));

            assertEquals(
                    Set.of("Valentina.Rojas", "Valentina.Rojas1"),
                    new HashSet<>(usernames));
        } finally {
            executor.shutdownNow();
        }
    }
}
