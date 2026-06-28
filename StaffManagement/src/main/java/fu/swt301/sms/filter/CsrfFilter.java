package fu.swt301.sms.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        // 1. Tạo CSRF Token nếu session chưa có
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        // 2. Chỉ kiểm tra token với các request làm thay đổi dữ liệu (POST, PUT, DELETE)
        if (req.getMethod().equalsIgnoreCase("POST")) {
            String requestToken = req.getParameter("csrfToken");
            String sessionToken = (String) session.getAttribute("csrfToken");

            // Nếu token không khớp hoặc bị trống, chặn request ngay lập tức
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Lỗi bảo mật CSRF: Token không hợp lệ!");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}