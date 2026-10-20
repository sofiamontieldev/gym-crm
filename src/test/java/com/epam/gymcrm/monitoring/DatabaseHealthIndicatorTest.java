package com.epam.gymcrm.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseHealthIndicatorTest {

    @Test
    void reportsUpAndClosesValidDatabaseConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getDatabaseProductName()).thenReturn("H2");

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("H2", health.getDetails().get("database"));
        verify(connection).close();
    }

    @Test
    void reportsDownAndClosesInvalidDatabaseConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(
                "Database connection validation failed",
                health.getDetails().get("reason"));
        verify(connection).close();
    }

    @Test
    void reportsDownWhenDatabaseConnectionCannotBeObtained() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("Database unavailable"));

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
