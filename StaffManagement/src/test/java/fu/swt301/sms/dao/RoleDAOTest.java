package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.utils.DBUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RoleDAOTest {

    private RoleDAO roleDAO;
    private MockedStatic<DBUtils> mockedDBUtils;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        roleDAO = new RoleDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock static method DBUtils.getConnection()
        mockedDBUtils = mockStatic(DBUtils.class);
        mockedDBUtils.when(DBUtils::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        if (mockedDBUtils != null) {
            mockedDBUtils.close();
        }
    }

    @Test
    public void testGetAllRoles_Success() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        when(mockResultSet.next()).thenReturn(true, true, false); // 2 rows
        when(mockResultSet.getInt("Role_ID")).thenReturn(1, 2);
        when(mockResultSet.getString("Role_Name")).thenReturn("Admin", "Staff");

        // Act
        List<Role> roles = roleDAO.getAllRoles();

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(2);
        assertThat(roles.get(0).getRoleID()).isEqualTo(1);
        assertThat(roles.get(0).getRoleName()).isEqualTo("Admin");
        assertThat(roles.get(1).getRoleID()).isEqualTo(2);
        assertThat(roles.get(1).getRoleName()).isEqualTo("Staff");
        
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).executeQuery();
    }
    
    @Test
    public void testGetAllRoles_EmptyResultSet() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // 0 rows

        // Act
        List<Role> roles = roleDAO.getAllRoles();

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty();
    }
    
    @Test
    public void testGetAllRoles_SQLException() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<Role> roles = roleDAO.getAllRoles();

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty(); // RoleDAO returns empty list on exception
    }
}
