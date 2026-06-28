package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StaffServiceTest {

    @Mock
    private StaffDAO staffDAO;

    // Đã xóa @InjectMocks ở đây
    private StaffService staffService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Tiêm mock trực tiếp vào Service thông qua constructor vừa tạo
        staffService = new StaffService(staffDAO);
    }

    // --- MODULE AUTH (ĐĂNG NHẬP) ---

    @Test
    public void testLogin_Success() throws Exception {
        String email = "test@fpt.edu.vn";
        String rawPassword = "password123";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Staff mockStaff = new Staff();
        mockStaff.setEmail(email);
        mockStaff.setPassword(hashedPassword);

        when(staffDAO.getStaffByEmail(email)).thenReturn(mockStaff);

        Staff result = staffService.login(email, rawPassword);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(staffDAO, times(1)).getStaffByEmail(email);
    }

    @Test
    public void testLogin_Fail_EmailNotFound() {
        String email = "wrong@fpt.edu.vn";
        when(staffDAO.getStaffByEmail(email)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            staffService.login(email, "anyPassword");
        });

        assertEquals("Tài khoản hoặc mật khẩu không chính xác.", exception.getMessage());
    }

    @Test
    public void testLogin_Fail_WrongPassword() {
        String email = "test@fpt.edu.vn";
        String rawPassword = "correctPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Staff mockStaff = new Staff();
        mockStaff.setEmail(email);
        mockStaff.setPassword(hashedPassword);

        when(staffDAO.getStaffByEmail(email)).thenReturn(mockStaff);

        Exception exception = assertThrows(Exception.class, () -> {
            staffService.login(email, "wrongPassword");
        });

        assertEquals("Tài khoản hoặc mật khẩu không chính xác.", exception.getMessage());
    }

    @Test
    public void testLogin_AccountLockoutAfterFiveAttempts() {
        String email = "bruteforce@fpt.edu.vn";
        when(staffDAO.getStaffByEmail(email)).thenReturn(null);

        for (int i = 0; i < 5; i++) {
            assertThrows(Exception.class, () -> {
                staffService.login(email, "wrongpass");
            });
        }

        Exception exception = assertThrows(Exception.class, () -> {
            staffService.login(email, "wrongpass");
        });

        assertTrue(exception.getMessage().contains("Tài khoản đang bị khóa tạm thời"));
    }

    // --- MODULE CRUD (QUẢN LÝ NHÂN VIÊN) ---

    @Test
    public void testGetStaffById_Found() {
        int staffId = 1;
        Staff mockStaff = new Staff();
        mockStaff.setStaffID(staffId);
        mockStaff.setFullName("Nguyen Van A");

        when(staffDAO.getStaffById(staffId)).thenReturn(mockStaff);

        Staff result = staffService.getStaffById(staffId);

        assertNotNull(result);
        assertEquals("Nguyen Van A", result.getFullName());
        verify(staffDAO, times(1)).getStaffById(staffId);
    }

    @Test
    public void testCreateStaff_WithPassword() {
        Staff newStaff = new Staff();
        newStaff.setEmail("newuser@fpt.edu.vn");
        newStaff.setPassword("rawPassword");

        staffService.createStaff(newStaff);

        assertNotEquals("rawPassword", newStaff.getPassword());
        assertTrue(newStaff.getPassword().startsWith("$2a$"));
        verify(staffDAO, times(1)).createStaff(newStaff);
    }

    @Test
    public void testUpdateStaff() {
        Staff updateStaff = new Staff();
        updateStaff.setStaffID(2);
        updateStaff.setFullName("Updated Name");

        staffService.updateStaff(updateStaff);
        verify(staffDAO, times(1)).updateStaff(updateStaff);
    }

    @Test
    public void testDeleteStaff() {
        int staffIdToDelete = 3;
        staffService.deleteStaff(staffIdToDelete);
        verify(staffDAO, times(1)).deleteStaff(staffIdToDelete);
    }

    @Test
    public void testCountStaff() {
        when(staffDAO.countStaffByFilter("1", "Nguyen", "IT")).thenReturn(10);
        int count = staffService.countStaff("1", "Nguyen", "IT");
        assertEquals(10, count);
        verify(staffDAO, times(1)).countStaffByFilter("1", "Nguyen", "IT");
    }

    @Test
    public void testGetStaffPaging() {
        int page = 2;
        int pageSize = 5;
        int offset = (page - 1) * pageSize;

        staffService.getStaffPaging(null, null, null, page, pageSize);
        verify(staffDAO, times(1)).getStaffByFilterPaging(null, null, null, offset, pageSize);
    }
}