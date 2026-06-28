package fu.swt301.sms.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Hủy Session hiện tại trên Server side
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 2. FR-04: Xóa triệt để Cookie JSESSIONID phía trình duyệt Client side
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0); // Đặt thời gian sống bằng 0 để trình duyệt xóa ngay lập tức
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(cookie);

        // 3. Điều hướng người dùng quay lại trang đăng nhập
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}