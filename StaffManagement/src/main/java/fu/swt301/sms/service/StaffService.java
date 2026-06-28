package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp dịch vụ xử lý các logic nghiệp vụ liên quan đến quản lý nhân viên và xác thực.
 * Đảm bảo các quy tắc phân quyền, bảo mật tài khoản và khóa tài khoản khi đăng nhập sai.
 */
public class StaffService {

    private StaffDAO staffDAO;

    // Lưu số lần đăng nhập sai và thời gian khóa tài khoản (Thread-safe)
    private static final Map<String, Integer> loginFailAttempts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lockTimeMap = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 5 * 60 * 1000; // 5 phút tính bằng mili-giây

    public StaffService() {
        this.staffDAO = new StaffDAO();
    }

    public StaffService(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    /**
     * Xác thực thông tin đăng nhập của người dùng.
     * Áp dụng cơ chế khóa tài khoản sau 5 lần sai và bảo mật thông báo lỗi chung.
     *
     * @param email    Email đăng nhập của nhân viên
     * @param password Mật khẩu chưa băm do người dùng nhập
     * @return Đối tượng Staff nếu đăng nhập thành công
     * @throws Exception Ngoại lệ kèm thông báo lỗi bảo mật chung hoặc thông báo khóa tài khoản
     */
    public Staff login(String email, String password) throws Exception {
        // Kiểm tra xem tài khoản có đang bị khóa hay không
        if (isAccountLocked(email)) {
            long timeLeft = (lockTimeMap.get(email) + LOCK_DURATION - System.currentTimeMillis()) / 1000;
            throw new Exception("Tài khoản đang bị khóa tạm thời. Vui lòng thử lại sau " + (timeLeft / 60 + 1) + " phút.");
        }

        Staff staff = staffDAO.getStaffByEmail(email);

        // FR-02: Sử dụng thông báo lỗi chung, không tiết lộ trường nào bị sai thông tin
        if (staff == null) {
            registerFailAttempt(email);
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
        }

        // Kiểm tra mật khẩu băm bằng jBCrypt
        if (!BCrypt.checkpw(password, staff.getPassword())) {
            registerFailAttempt(email);
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
        }

        // Đăng nhập thành công -> Reset bộ đếm lỗi
        resetLoginAttempts(email);
        return staff;
    }

    /**
     * Tính toán tổng số lượng nhân viên thỏa mãn điều kiện tìm kiếm để phục vụ phân trang.
     *
     * @param searchId   Từ khóa tìm kiếm theo ID
     * @param searchName Từ khóa tìm kiếm theo Tên
     * @param searchDept Từ khóa tìm kiếm theo Phòng ban
     * @return Tổng số lượng nhân viên tìm thấy
     */
    public int countStaff(String searchId, String searchName, String searchDept) {
        return staffDAO.countStaffByFilter(searchId, searchName, searchDept);
    }

    /**
     * Lấy danh sách nhân viên kết hợp phân trang và tìm kiếm nâng cao.
     *
     * @param searchId   Từ khóa tìm kiếm theo ID
     * @param searchName Từ khóa tìm kiếm theo Tên
     * @param searchDept Từ khóa tìm kiếm theo Phòng ban
     * @param page       Trang hiện tại (bắt đầu từ 1)
     * @param pageSize   Số lượng bản ghi trên một trang
     * @return Danh sách nhân viên thỏa mãn điều kiện
     */
    public List<Staff> getStaffPaging(String searchId, String searchName, String searchDept, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return staffDAO.getStaffByFilterPaging(searchId, searchName, searchDept, offset, pageSize);
    }

    /**
     * Lấy thông tin chi tiết của một nhân viên dựa trên ID.
     *
     * @param staffId Mã định danh của nhân viên
     * @return Đối tượng Staff đầy đủ thông tin hoặc null nếu không tồn tại
     */
    public Staff getStaffById(int staffId) {
        return staffDAO.getStaffById(staffId);
    }

    /**
     * Thêm mới một nhân viên vào hệ thống. Thực hiện băm mật khẩu tự động trước khi lưu.
     *
     * @param staff Đối tượng nhân viên chứa thông tin cần thêm mới
     */
    public void createStaff(Staff staff) {
        // FR-01: Mã hóa mật khẩu bằng bCrypt trước khi đưa xuống cơ sở dữ liệu
        if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
            String hashedPw = BCrypt.hashpw(staff.getPassword(), BCrypt.gensalt());
            staff.setPassword(hashedPw);
        }
        staffDAO.createStaff(staff);
    }

    /**
     * Cập nhật thông tin của nhân viên hiện tại trong hệ thống.
     *
     * @param staff Đối tượng nhân viên chứa các thông tin cập nhật mới
     */
    public void updateStaff(Staff staff) {
        staffDAO.updateStaff(staff);
    }

    /**
     * Xóa hoàn toàn bản ghi của một nhân viên ra khỏi hệ thống (Hard Delete).
     *
     * @param staffId Mã định danh của nhân viên cần xóa
     */
    public void deleteStaff(int staffId) {
        staffDAO.deleteStaff(staffId);
    }

    // --- Các phương thức bổ trợ private (phục vụ chức năng khóa tài khoản) ---

    private boolean isAccountLocked(String email) {
        if (!lockTimeMap.containsKey(email)) return false;
        if (System.currentTimeMillis() - lockTimeMap.get(email) > LOCK_DURATION) {
            resetLoginAttempts(email);
            return false;
        }
        return true;
    }

    private void registerFailAttempt(String email) {
        int attempts = loginFailAttempts.getOrDefault(email, 0) + 1;
        loginFailAttempts.put(email, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            lockTimeMap.put(email, System.currentTimeMillis());
        }
    }

    private void resetLoginAttempts(String email) {
        loginFailAttempts.remove(email);
        lockTimeMap.remove(email);
    }
}