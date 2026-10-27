package com.epam.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebApplicationContextTest {

    @Test
    void startsAsAWebApplication() {
        // Context startup is the assertion.
    }
}
