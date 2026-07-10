package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffServiceTest {

    @Mock
    private StaffDAO staffDAO;

    private StaffService staffService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        staffService = new StaffService(staffDAO);
        resetLockoutMaps();
    }
    
    @SuppressWarnings("unchecked")
    private void resetLockoutMaps() throws Exception {
        Field attemptsField = StaffService.class.getDeclaredField("loginFailAttempts");
        attemptsField.setAccessible(true);
        Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(null);
        attempts.clear();
        
        Field lockTimeField = StaffService.class.getDeclaredField("lockTimeMap");
        lockTimeField.setAccessible(true);
        Map<String, Long> lockTime = (Map<String, Long>) lockTimeField.get(null);
        lockTime.clear();
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

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(staffDAO, times(1)).getStaffByEmail(email);
    }

    @Test
    public void testLogin_Fail_EmailNotFound() throws Exception {
        String email = "wrong@fpt.edu.vn";
        when(staffDAO.getStaffByEmail(email)).thenReturn(null);

        assertThatThrownBy(() -> staffService.login(email, "anyPassword"))
                .isInstanceOf(Exception.class)
                .hasMessage("Tài khoản hoặc mật khẩu không chính xác.");
    }

    @Test
    public void testLogin_Fail_WrongPassword() throws Exception {
        String email = "test@fpt.edu.vn";
        String rawPassword = "correctPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Staff mockStaff = new Staff();
        mockStaff.setEmail(email);
        mockStaff.setPassword(hashedPassword);

        when(staffDAO.getStaffByEmail(email)).thenReturn(mockStaff);

        assertThatThrownBy(() -> staffService.login(email, "wrongPassword"))
                .isInstanceOf(Exception.class)
                .hasMessage("Tài khoản hoặc mật khẩu không chính xác.");
    }

    @Test
    public void testLogin_AccountLockoutAfterFiveAttempts() throws Exception {
        String email = "bruteforce@fpt.edu.vn";
        when(staffDAO.getStaffByEmail(email)).thenReturn(null);

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> staffService.login(email, "wrongpass"))
                    .isInstanceOf(Exception.class);
        }

        assertThatThrownBy(() -> staffService.login(email, "wrongpass"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Tài khoản đang bị khóa tạm thời");
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

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
        verify(staffDAO, times(1)).getStaffById(staffId);
    }

    @Test
    public void testCreateStaff_WithPassword() {
        Staff newStaff = new Staff();
        newStaff.setEmail("newuser@fpt.edu.vn");
        newStaff.setPassword("rawPassword");

        staffService.createStaff(newStaff);

        assertThat(newStaff.getPassword()).isNotEqualTo("rawPassword");
        assertThat(newStaff.getPassword()).startsWith("$2a$");
        verify(staffDAO, times(1)).createStaff(newStaff);
    }
    
    @Test
    public void testCreateStaff_NullPassword() {
        Staff newStaff = new Staff();
        newStaff.setEmail("newuser@fpt.edu.vn");
        newStaff.setPassword(null);

        staffService.createStaff(newStaff);
        
        assertThat(newStaff.getPassword()).isNull();
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
    public void testUpdateStaff_Null() {
        staffService.updateStaff(null);
        verify(staffDAO, times(1)).updateStaff(null);
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
        assertThat(count).isEqualTo(10);
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
    
    @Test
    public void testGetStaffPaging_NegativePage() {
        int page = -1;
        int pageSize = 5;
        int offset = (page - 1) * pageSize;
        
        staffService.getStaffPaging(null, null, null, page, pageSize);
        verify(staffDAO, times(1)).getStaffByFilterPaging(null, null, null, offset, pageSize);
    }
}