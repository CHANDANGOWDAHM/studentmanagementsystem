-- Create database
CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin', 'user') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    course VARCHAR(50) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    enrollment_date DATE,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, full_name, role) VALUES
('admin', 'admin@example.com', 'admin123', 'System Administrator', 'admin');

-- Insert sample users
INSERT INTO users (username, email, password, full_name, role) VALUES
('john_doe', 'john.doe@example.com', 'password123', 'John Doe', 'user'),
('jane_smith', 'jane.smith@example.com', 'password123', 'Jane Smith', 'user');

-- Insert sample student data
INSERT INTO students (name, email, course, phone, address, enrollment_date, user_id) VALUES
('Alice Johnson', 'alice@example.com', 'Computer Science', '1234567890', '123 Main St, City', '2024-01-15', 1),
('Bob Williams', 'bob@example.com', 'Mathematics', '0987654321', '456 Oak Ave, Town', '2024-01-20', 2),
('Charlie Brown', 'charlie@example.com', 'Physics', '1122334455', '789 Pine Rd, Village', '2024-02-01', 2),
('Diana Prince', 'diana@example.com', 'Engineering', '5566778899', '321 Elm St, City', '2024-02-10', 1);