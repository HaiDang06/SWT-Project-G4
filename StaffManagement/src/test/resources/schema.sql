DROP TABLE IF EXISTS Staff;
DROP TABLE IF EXISTS Role;

CREATE TABLE IF NOT EXISTS Role (
    Role_ID INT PRIMARY KEY,
    Role_Name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Staff (
    StaffID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Gender BOOLEAN,
    PhoneNumber VARCHAR(20),
    Email VARCHAR(100) UNIQUE NOT NULL,
    Password VARCHAR(100) NOT NULL,
    Role_ID INT,
    IsActive BOOLEAN,
    Dob DATE,
    Department VARCHAR(100),
    Position VARCHAR(100),
    Salary DOUBLE,
    HireDate DATE,
    FOREIGN KEY (Role_ID) REFERENCES Role(Role_ID)
);

-- Seed initial roles
INSERT INTO Role (Role_ID, Role_Name) VALUES (1, 'ADMIN');
INSERT INTO Role (Role_ID, Role_Name) VALUES (2, 'USER');
