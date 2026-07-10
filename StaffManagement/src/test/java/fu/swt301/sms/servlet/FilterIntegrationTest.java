package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.filter.AuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class FilterIntegrationTest {

    private AuthFilter authFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        authFilter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/sms");
    }

    @Test
    public void testPublicUrls_Allowed() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/login");
        
        authFilter.doFilter(request, response, filterChain);

        // Should call chain.doFilter and not redirect
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    public void testProtectedUrl_NoSession_RedirectToLogin() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/staff-list");
        when(request.getSession(false)).thenReturn(null);

        authFilter.doFilter(request, response, filterChain);

        verify(response, times(1)).sendRedirect("/sms/login");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    public void testProtectedUrl_WithSessionUser_Allowed() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/staff-list");
        when(request.getSession(false)).thenReturn(session);

        Staff user = new Staff();
        Role role = new Role(); role.setRoleName("USER");
        user.setRole(role);
        when(session.getAttribute("user")).thenReturn(user);

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testCrudUrl_WithSessionUSER_Forbidden() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/staff-crud");
        when(request.getSession(false)).thenReturn(session);

        Staff user = new Staff();
        Role role = new Role(); role.setRoleName("USER");
        user.setRole(role);
        when(session.getAttribute("user")).thenReturn(user);

        authFilter.doFilter(request, response, filterChain);

        verify(response, times(1)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    public void testCrudUrl_WithSessionADMIN_Allowed() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/staff-crud");
        when(request.getSession(false)).thenReturn(session);

        Staff user = new Staff();
        Role role = new Role(); role.setRoleName("ADMIN");
        user.setRole(role);
        when(session.getAttribute("user")).thenReturn(user);

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
