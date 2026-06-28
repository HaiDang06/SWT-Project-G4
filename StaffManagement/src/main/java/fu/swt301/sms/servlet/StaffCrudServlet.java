package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/staff-crud")
public class StaffCrudServlet extends HttpServlet {
    private StaffService staffService;
    private RoleDAO roleDAO;

    @Override
    public void init() {
        this.staffService = new StaffService();
        this.roleDAO = new RoleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            request.setAttribute("roleList", roleDAO.getAllRoles());
            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
        } else if ("edit".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("staff", staffService.getStaffById(id));
            request.setAttribute("roleList", roleDAO.getAllRoles());
            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
        } else if ("view".equals(action)) { // FR-10: Xem chi tiết
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("staff", staffService.getStaffById(id));
            request.getRequestDispatcher("staff-detail.jsp").forward(request, response);
        } else {
            response.sendRedirect("staff-list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            staffService.deleteStaff(id);
            response.sendRedirect("staff-list");
            return;
        }

        // Đọc dữ liệu từ form (FR-07)
        Staff staff = new Staff();
        if (request.getParameter("staffID") != null) {
            staff.setStaffID(Integer.parseInt(request.getParameter("staffID")));
        }
        staff.setFullName(request.getParameter("fullName"));
        staff.setGender(Boolean.parseBoolean(request.getParameter("gender")));
        staff.setPhoneNumber(request.getParameter("phoneNumber"));
        staff.setEmail(request.getParameter("email"));
        staff.setPassword(request.getParameter("password")); // Chỉ dùng khi tạo mới
        staff.setIsActive(Boolean.parseBoolean(request.getParameter("isActive")));

        // Xử lý 5 trường mới
        if(request.getParameter("dob") != null && !request.getParameter("dob").isEmpty())
            staff.setDob(LocalDate.parse(request.getParameter("dob")));
        if(request.getParameter("hireDate") != null && !request.getParameter("hireDate").isEmpty())
            staff.setHireDate(LocalDate.parse(request.getParameter("hireDate")));
        staff.setDepartment(request.getParameter("department"));
        staff.setPosition(request.getParameter("position"));
        if(request.getParameter("salary") != null && !request.getParameter("salary").isEmpty())
            staff.setSalary(Double.parseDouble(request.getParameter("salary")));

        Role role = new Role();
        role.setRoleID(Integer.parseInt(request.getParameter("roleID")));
        staff.setRole(role);

        if ("create".equals(action)) {
            staffService.createStaff(staff);
        } else if ("update".equals(action)) {
            staffService.updateStaff(staff);
        }
        response.sendRedirect("staff-list");
    }
}