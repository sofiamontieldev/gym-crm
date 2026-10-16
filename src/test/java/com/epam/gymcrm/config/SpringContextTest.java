package com.epam.gymcrm.config;

import com.epam.gymcrm.facade.GymCrmFacade;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(AppConfig.class)
class SpringContextTest {

    @Autowired
    private GymCrmFacade facade;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    @Test
    void springContextStartsWithHibernateInfrastructure() {
        assertNotNull(facade);
        assertNotNull(sessionFactory);
        assertNotNull(transactionManager);
        assertNotNull(dataSource);
    }

    @Test
    void hibernateCreatesTheExpectedTables() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
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
