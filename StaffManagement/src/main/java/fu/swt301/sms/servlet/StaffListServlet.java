package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/staff-list")
public class StaffListServlet extends HttpServlet {
    private StaffService staffService;

    @Override
    public void init() {
        this.staffService = new StaffService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchId = request.getParameter("searchId");
        String searchName = request.getParameter("searchName");
        String searchDept = request.getParameter("searchDept");

        // Cấu hình phân trang
        int page = 1;
        int pageSize = 5; // Số bản ghi trên 1 trang
        if (request.getParameter("page") != null) {
            try { page = Integer.parseInt(request.getParameter("page")); } catch (Exception e) {}
        }

        int totalRecords = staffService.countStaff(searchId, searchName, searchDept);
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        List<Staff> staffList = staffService.getStaffPaging(searchId, searchName, searchDept, page, pageSize);

        request.setAttribute("staffList", staffList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        // Lưu lại tham số tìm kiếm để hiển thị lên URL và giao diện
        request.setAttribute("searchId", searchId);
        request.setAttribute("searchName", searchName);
        request.setAttribute("searchDept", searchDept);

        request.getRequestDispatcher("staff-list.jsp").forward(request, response);
    }
}