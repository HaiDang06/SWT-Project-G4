package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
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

public class StaffDAOTest {

    private StaffDAO staffDAO;
    private MockedStatic<DBUtils> mockedDBUtils;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        staffDAO = new StaffDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDBUtils = mockStatic(DBUtils.class);
        mockedDBUtils.when(DBUtils::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        if (mockedDBUtils != null) {
            mockedDBUtils.close();
        }
    }

    // --- Helper setup for ResultSet mapping ---
    private void setupMockResultSetForStaff() throws SQLException {
        when(mockResultSet.getInt("StaffID")).thenReturn(1);
        when(mockResultSet.getString("FullName")).thenReturn("Nguyen Van A");
        when(mockResultSet.getBoolean("Gender")).thenReturn(true);
        when(mockResultSet.getString("PhoneNumber")).thenReturn("0123456789");
        when(mockResultSet.getString("Email")).thenReturn("test@test.com");
        when(mockResultSet.getString("Password")).thenReturn("hashedPass");
        when(mockResultSet.getBoolean("IsActive")).thenReturn(true);
        when(mockResultSet.getDate("Dob")).thenReturn(java.sql.Date.valueOf("1990-01-01"));
        when(mockResultSet.getString("Department")).thenReturn("IT");
        when(mockResultSet.getString("Position")).thenReturn("Dev");
        when(mockResultSet.getDouble("Salary")).thenReturn(1000.0);
        when(mockResultSet.getDate("HireDate")).thenReturn(java.sql.Date.valueOf("2020-01-01"));
        when(mockResultSet.getInt("Role_ID")).thenReturn(2);
        when(mockResultSet.getString("Role_Name")).thenReturn("Staff");
    }

    @Test
    public void testIsEmailExists_True() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // count > 0

        boolean exists = staffDAO.isEmailExists("test@test.com", 0);

        assertThat(exists).isTrue();
    }

    @Test
    public void testIsEmailExists_False() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // count == 0

        boolean exists = staffDAO.isEmailExists("test@test.com", 0);

        assertThat(exists).isFalse();
    }
    
    @Test
    public void testIsFullNameExists() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        boolean exists = staffDAO.isFullNameExists("Nguyen Van A", 0);

        assertThat(exists).isTrue();
    }

    @Test
    public void testGetStaffById_Found() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockResultSetForStaff();

        Staff staff = staffDAO.getStaffById(1);

        assertThat(staff).isNotNull();
        assertThat(staff.getStaffID()).isEqualTo(1);
        assertThat(staff.getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    public void testGetStaffById_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Staff staff = staffDAO.getStaffById(99);

        assertThat(staff).isNull();
    }

    @Test
    public void testGetStaffByEmail() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockResultSetForStaff();

        Staff staff = staffDAO.getStaffByEmail("test@test.com");

        assertThat(staff).isNotNull();
        assertThat(staff.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    public void testCreateStaff_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Staff staff = new Staff();
        staff.setFullName("Nguyen Van B");
        Role role = new Role();
        role.setRoleID(1);
        staff.setRole(role);
        // other fields can be null, handled by DAO

        staffDAO.createStaff(staff);

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateStaff_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Staff staff = new Staff();
        staff.setStaffID(1);
        staff.setFullName("Nguyen Van C");
        Role role = new Role();
        role.setRoleID(1);
        staff.setRole(role);

        staffDAO.updateStaff(staff);

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteStaff_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        staffDAO.deleteStaff(1);

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testCountStaffByFilter() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);

        int count = staffDAO.countStaffByFilter("1", "Nguyen", "IT");

        assertThat(count).isEqualTo(5);
    }

    @Test
    public void testGetStaffByFilterPaging() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockResultSetForStaff();

        List<Staff> list = staffDAO.getStaffByFilterPaging("1", "Nguyen", "IT", 0, 10);

        assertThat(list).isNotNull();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStaffID()).isEqualTo(1);
    }
    
    @Test
    public void testGetStaffByFilterPaging_EmptyFilters() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockResultSetForStaff();

        List<Staff> list = staffDAO.getStaffByFilterPaging(null, "", null, 0, 10);

        assertThat(list).isNotNull();
        assertThat(list).hasSize(1);
        verify(mockConnection).prepareStatement(contains("ORDER BY"));
    }
}
