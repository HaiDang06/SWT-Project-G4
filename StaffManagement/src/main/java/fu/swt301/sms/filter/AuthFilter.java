package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Staff;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Áp dụng lên toàn bộ URL
@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();

        // Bỏ qua lọc cho các file CSS, JS hoặc trang Login/Logout
        if (path.startsWith("/login") || path.startsWith("/logout") || path.contains(".css") || path.contains(".js")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Staff user = (session != null) ? (Staff) session.getAttribute("user") : null;

        // Bắt buộc đăng nhập
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // FR-05: Chỉ ADMIN mới được vào trang CRUD (Thêm, Sửa, Xóa)
        if (path.startsWith("/staff-crud")) {
            if (!"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only ADMIN can perform this action.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}