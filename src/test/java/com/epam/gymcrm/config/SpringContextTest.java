package com.epam.gymcrm.config;

import com.epam.gymcrm.facade.GymCrmFacade;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class SpringContextTest {

    private final GymCrmFacade facade;
    private final SessionFactory sessionFactory;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    SpringContextTest(
            GymCrmFacade facade,
            SessionFactory sessionFactory,
            PlatformTransactionManager transactionManager,
            DataSource dataSource,
            JdbcTemplate jdbcTemplate) {

        this.facade = facade;
        this.sessionFactory = sessionFactory;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void springContextStartsWithHibernateInfrastructure() {
        assertNotNull(facade);
        assertNotNull(sessionFactory);
        assertNotNull(transactionManager);
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @Test
    void hibernateCreatesTheExpectedTables() {
        List<String> expectedTables = List.of(
                "USERS",
                "TRAINEES",
                "TRAINERS",
                "TRAININGS",
                "TRAINING_TYPES",
                "TRAINEE2TRAINER");

        for (String table : expectedTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from information_schema.tables " +
                            "where table_schema = 'PUBLIC' and table_name = ?",
                    Integer.class,
                    table);
            assertEquals(1, count, "Expected Hibernate table " + table);
        }
    }
}
