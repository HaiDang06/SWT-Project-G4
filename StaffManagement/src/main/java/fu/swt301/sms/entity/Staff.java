package fu.swt301.sms.entity;

import java.time.LocalDate;

public class Staff {
    private int staffID;
    private String fullName;
    private boolean gender;
    private String phoneNumber;
    private String email;
    private String password;
    private Role role;
    private boolean isActive;

    // FR-07: Bổ sung 5 trường dữ liệu bắt buộc
    private LocalDate dob; // Ngày sinh
    private String department; // Phòng ban
    private String position; // Chức vụ
    private double salary; // Mức lương
    private LocalDate hireDate; // Ngày vào làm

    // Constructors
    public Staff() {}

    // Getters and Setters (Bạn có thể dùng tính năng Generate của IDE để tạo nhanh)
    public int getStaffID() { return staffID; }
    public void setStaffID(int staffID) { this.staffID = staffID; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public boolean isGender() { return gender; }
    public void setGender(boolean gender) { this.gender = gender; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}