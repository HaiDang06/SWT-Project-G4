package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.integration.BaseIntegrationTest;
import fu.swt301.sms.service.StaffService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeIntegrationTest extends BaseIntegrationTest {

    private StaffCrudServlet staffCrudServlet;
    private StaffListServlet staffListServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher requestDispatcher;
    private StaffService staffService;

    @BeforeEach
    public void setUp() {
        staffCrudServlet = new StaffCrudServlet();
        staffCrudServlet.init();

        staffListServlet = new StaffListServlet();
        staffListServlet.init();

        staffService = new StaffService();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getContextPath()).thenReturn("");
    }

    @Test
    public void testCreateStaff_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("fullName")).thenReturn("Jane Smith");
        when(request.getParameter("gender")).thenReturn("true");
        when(request.getParameter("phoneNumber")).thenReturn("0987654321");
        when(request.getParameter("email")).thenReturn("jane.smith@example.com");
        when(request.getParameter("password")).thenReturn("pwd123");
        when(request.getParameter("isActive")).thenReturn("true");
        when(request.getParameter("dob")).thenReturn("1995-01-01");
        when(request.getParameter("department")).thenReturn("HR");
        when(request.getParameter("position")).thenReturn("Manager");
        when(request.getParameter("salary")).thenReturn("1500.0");
        when(request.getParameter("hireDate")).thenReturn("2020-05-05");
        when(request.getParameter("roleID")).thenReturn("2");

        staffCrudServlet.doPost(request, response);

        verify(response, times(1)).sendRedirect("staff-list");

        // Verify in database
        String sql = "SELECT * FROM Staff WHERE Email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "jane.smith@example.com");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Staff should be inserted into the database");
            assertEquals("Jane Smith", rs.getString("FullName"));
            assertEquals("HR", rs.getString("Department"));
        }
    }

    @Test
    public void testUpdateStaff_Success() throws ServletException, IOException, SQLException {
        // Seed a staff
        Staff staff = new Staff();
        staff.setFullName("Old Name");
        staff.setEmail("old.name@example.com");
        staff.setPassword("pass");
        staff.setGender(true);
        staff.setIsActive(true);
        Role r = new Role(); r.setRoleID(2);
        staff.setRole(r);
        staffService.createStaff(staff);

        // Get the inserted staff ID
        Staff inserted = staffService.getStaffPaging(null, "Old Name", null, 1, 10).get(0);

        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("staffID")).thenReturn(String.valueOf(inserted.getStaffID()));
        when(request.getParameter("fullName")).thenReturn("New Name");
        when(request.getParameter("gender")).thenReturn("false");
        when(request.getParameter("phoneNumber")).thenReturn("111222333");
        when(request.getParameter("email")).thenReturn("old.name@example.com"); // Keep email
        when(request.getParameter("isActive")).thenReturn("true");
        when(request.getParameter("roleID")).thenReturn("2");

        staffCrudServlet.doPost(request, response);

        verify(response, times(1)).sendRedirect("staff-list");

        Staff updated = staffService.getStaffById(inserted.getStaffID());
        assertEquals("New Name", updated.getFullName());
        assertFalse(updated.isGender());
    }

    @Test
    public void testDeleteStaff_Success() throws ServletException, IOException {
        // Seed a staff
        Staff staff = new Staff();
        staff.setFullName("To Be Deleted");
        staff.setEmail("delete.me@example.com");
        staff.setPassword("pass");
        Role r = new Role(); r.setRoleID(2);
        staff.setRole(r);
        staffService.createStaff(staff);

        Staff inserted = staffService.getStaffPaging(null, "To Be Deleted", null, 1, 10).get(0);

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn(String.valueOf(inserted.getStaffID()));

        staffCrudServlet.doPost(request, response);

        verify(response, times(1)).sendRedirect("staff-list");

        Staff deleted = staffService.getStaffById(inserted.getStaffID());
        assertNull(deleted, "Staff should be deleted from DB");
    }

    @Test
    public void testStaffListServlet_DoGet() throws ServletException, IOException {
        // Seed 2 staff members
        Staff s1 = new Staff(); s1.setFullName("Staff One"); s1.setEmail("one@example.com"); s1.setPassword("1"); Role r1 = new Role(); r1.setRoleID(2); s1.setRole(r1);
        Staff s2 = new Staff(); s2.setFullName("Staff Two"); s2.setEmail("two@example.com"); s2.setPassword("2"); Role r2 = new Role(); r2.setRoleID(2); s2.setRole(r2);
        staffService.createStaff(s1);
        staffService.createStaff(s2);

        when(request.getParameter("page")).thenReturn("1");

        staffListServlet.doGet(request, response);

        verify(request, times(1)).setAttribute(eq("staffList"), any(List.class));
        verify(request, times(1)).setAttribute(eq("currentPage"), eq(1));
        verify(request, times(1)).setAttribute(eq("totalPages"), anyInt());
        verify(requestDispatcher, times(1)).forward(request, response);
    }

    @Test
    public void testViewStaff_DoGet() throws ServletException, IOException {
        // Seed a staff
        Staff staff = new Staff();
        staff.setFullName("View Me");
        staff.setEmail("view.me@example.com");
        staff.setPassword("pass");
        Role r = new Role(); r.setRoleID(2);
        staff.setRole(r);
        staffService.createStaff(staff);

        Staff inserted = staffService.getStaffPaging(null, "View Me", null, 1, 10).get(0);

        when(request.getParameter("action")).thenReturn("view");
        when(request.getParameter("id")).thenReturn(String.valueOf(inserted.getStaffID()));

        staffCrudServlet.doGet(request, response);

        verify(request, times(1)).setAttribute(eq("staff"), any(Staff.class));
        verify(requestDispatcher, times(1)).forward(request, response);
    }
}
