package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.integration.BaseIntegrationTest;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class AuthIntegrationTest extends BaseIntegrationTest {

    private LoginServlet loginServlet;
    private LogoutServlet logoutServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    public void setUp() throws ServletException, SQLException {
        loginServlet = new LoginServlet();
        loginServlet.init();

        logoutServlet = new LogoutServlet();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getContextPath()).thenReturn("");

        // Seed a user into H2 database for testing auth
        String insertStaff = "INSERT INTO Staff (FullName, Email, Password, Role_ID, IsActive) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertStaff)) {
            ps.setString(1, "John Doe");
            ps.setString(2, "john.doe@example.com");
            ps.setString(3, BCrypt.hashpw("password123", BCrypt.gensalt()));
            ps.setInt(4, 1); // ADMIN
            ps.setBoolean(5, true);
            ps.executeUpdate();
        }
    }

    @Test
    public void testLogin_Success() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("john.doe@example.com");
        when(request.getParameter("password")).thenReturn("password123");

        loginServlet.doPost(request, response);

        // Verify session is created and staff is set
        verify(request, atLeastOnce()).getSession();
        verify(session, times(1)).setAttribute(eq("user"), any(Staff.class));
        
        // Verify redirect to staff-list
        verify(response, times(1)).sendRedirect("staff-list");
    }

    @Test
    public void testLogin_Failure_WrongPassword() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("john.doe@example.com");
        when(request.getParameter("password")).thenReturn("wrongpassword");

        loginServlet.doPost(request, response);

        // Verify no session user is set
        verify(session, never()).setAttribute(eq("user"), any());

        // Verify error attribute is set
        verify(request, times(1)).setAttribute(eq("error"), anyString());

        // Verify forward to login.jsp
        verify(requestDispatcher, times(1)).forward(request, response);
    }

    @Test
    public void testLogin_Failure_UserNotFound() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("unknown@example.com");
        when(request.getParameter("password")).thenReturn("password123");

        loginServlet.doPost(request, response);

        verify(session, never()).setAttribute(eq("user"), any());
        verify(request, times(1)).setAttribute(eq("error"), anyString());
        verify(requestDispatcher, times(1)).forward(request, response);
    }

    @Test
    public void testLogin_LockoutAfter5FailedAttempts() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("john.doe@example.com");
        when(request.getParameter("password")).thenReturn("wrongpassword");

        // Fail 5 times
        for (int i = 0; i < 5; i++) {
            loginServlet.doPost(request, response);
        }

        // The 6th time should throw a lockout error message even if password is correct
        when(request.getParameter("password")).thenReturn("password123");
        loginServlet.doPost(request, response);

        // We check if the last attribute set was the lockout message
        // The message is "Tài khoản đang bị khóa tạm thời. Vui lòng thử lại sau X phút."
        verify(request, atLeast(1)).setAttribute(eq("error"), contains("bị khóa"));
        
        // We never set user to session because it's locked
        verify(session, never()).setAttribute(eq("user"), any());
    }

    @Test
    public void testLogout() throws ServletException, IOException {
        logoutServlet.doGet(request, response);

        // Verify session is invalidated
        verify(session, times(1)).invalidate();

        // Verify redirect to login.jsp
        verify(response, times(1)).sendRedirect("/login.jsp");
    }
}
