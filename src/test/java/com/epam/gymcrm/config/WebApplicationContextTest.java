package com.epam.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebApplicationContextTest {

    @Test
    void startsAsAWebApplication() {
        // Context startup is the assertion.
    }
}
