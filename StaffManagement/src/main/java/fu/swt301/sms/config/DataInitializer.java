package fu.swt301.sms.config;

import fu.swt301.sms.utils.DBUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebListener
public class DataInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DBUtils.getConnection()) {
            System.out.println("Checking database schema for PostgreSQL...");

            // Sử dụng lệnh tạo bảng an toàn của PostgreSQL
            createRoleTable(conn);
            createStaffTable(conn);

            // Kiểm tra xem dữ liệu đã tồn tại chưa
            boolean dataExists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Role");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    dataExists = true;
                }
            }

            if (!dataExists) {
                System.out.println("No data found. Initializing default data with BCrypt...");
                insertDefaultData(conn);
            } else {
                System.out.println("Data already exists. Skipping initialization.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database.", e);
        }
    }

    private void createRoleTable(Connection conn) throws SQLException {
        // Sử dụng câu lệnh sinh bảng chuẩn hóa PostgreSQL
        String createSQL = "CREATE TABLE IF NOT EXISTS Role (" +
                "Role_ID INT PRIMARY KEY, " +
                "Role_Name VARCHAR(50) NOT NULL UNIQUE" +
                ")";
        try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
            ps.execute();
        }
    }

    private void createStaffTable(Connection conn) throws SQLException {
        // Đã khắc phục: Chuyển SERIAL, VARCHAR, BOOLEAN chuẩn Postgres
        // Đã khắc phục: Bổ sung đầy đủ 5 trường thông tin theo yêu cầu FR-07
        String createSQL = "CREATE TABLE IF NOT EXISTS Staff (" +
                "StaffID SERIAL PRIMARY KEY, " +
                "FullName VARCHAR(100) NOT NULL, " +
                "Gender BOOLEAN NOT NULL, " +
                "PhoneNumber VARCHAR(20), " +
                "Email VARCHAR(100) NOT NULL UNIQUE, " +
                "Password VARCHAR(255) NOT NULL, " +
                "Role_ID INT NOT NULL, " +
                "IsActive BOOLEAN NOT NULL, " +
                "Dob DATE, " +
                "Department VARCHAR(100), " +
                "Position VARCHAR(100), " +
                "Salary DOUBLE PRECISION, " +
                "HireDate DATE, " +
                "CONSTRAINT FK_Staff_Role FOREIGN KEY (Role_ID) REFERENCES Role(Role_ID)" +
                ")";
        try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
            ps.execute();
        }
    }

    private void insertDefaultData(Connection conn) throws SQLException {
        // Chèn các quyền hệ thống mặc định dạng viết hoa đồng bộ với Filter
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Role (Role_ID, Role_Name) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            ps.setInt(1, 1);
            ps.setString(2, "ADMIN");
            ps.addBatch();

            ps.setInt(1, 2);
            ps.setString(2, "USER");
            ps.addBatch();

            ps.executeBatch();
        }

        // Đạt yêu cầu FR-01: Băm mật khẩu bằng thuật toán BCrypt trước khi hạt giống (seed) vào DB
        String rawPassword = "admin123";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        String insertStaffSQL = "INSERT INTO Staff (FullName, Gender, PhoneNumber, Email, Password, Role_ID, IsActive, Dob, Department, Position, Salary, HireDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";

        try (PreparedStatement ps = conn.prepareStatement(insertStaffSQL)) {
            ps.setString(1, "Admin User");
            ps.setBoolean(2, true); // Male
            ps.setString(3, "0123456789");
            ps.setString(4, "admin@example.com");
            ps.setString(5, hashedPassword); // Mật khẩu đã được mã hóa bảo mật
            ps.setInt(6, 1); // Quyền ADMIN
            ps.setBoolean(7, true); // Active
            ps.setDate(8, java.sql.Date.valueOf("2000-11-14"));
            ps.setString(9, "Human Resources");
            ps.setString(10, "HR Manager");
            ps.setDouble(11, 3000.0);
            ps.setDate(12, java.sql.Date.valueOf("2026-01-01"));
            ps.executeUpdate();
            System.out.println("Default admin user inserted successfully.");
        }
    }
}