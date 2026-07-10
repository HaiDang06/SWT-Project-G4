package fu.swt301.sms.integration;

import fu.swt301.sms.utils.DBUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseIntegrationTest {

    protected static Connection connection;
    protected static MockedStatic<DBUtils> mockedDBUtils;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        // Initialize H2 Database connection
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:StaffDB;DB_CLOSE_DELAY=-1", "postgres", "123");

        // Run schema.sql to create tables and insert initial roles
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("RUNSCRIPT FROM 'classpath:schema.sql'");
        }

        // Mock DBUtils to return H2 connection
        mockedDBUtils = Mockito.mockStatic(DBUtils.class);
        mockedDBUtils.when(DBUtils::getConnection).thenAnswer(invocation -> 
            DriverManager.getConnection("jdbc:h2:mem:StaffDB;DB_CLOSE_DELAY=-1", "postgres", "123")
        );
    }

    @AfterAll
    public static void tearDownDatabase() throws Exception {
        if (mockedDBUtils != null) {
            mockedDBUtils.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    public void cleanUpData() throws SQLException {
        // Clear data between tests to ensure test isolation
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Staff");
            // Reset auto-increment
            stmt.execute("ALTER TABLE Staff ALTER COLUMN StaffID RESTART WITH 1");
        }
    }
}
