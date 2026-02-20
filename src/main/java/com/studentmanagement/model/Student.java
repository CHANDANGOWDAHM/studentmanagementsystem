package com.studentmanagement.model;

import java.util.Date;

public class Student {
    private int id;
    private String name;
    private String email;
    private String course;
    private String phone;
    private String address;
    private Date enrollmentDate;
    private int userId;
    private Date createdAt;
    private Date updatedAt;

    // Constructors
    public Student() {}

    public Student(String name, String email, String course, String phone,
                   String address, Date enrollmentDate, int userId) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.phone = phone;
        this.address = address;
        this.enrollmentDate = enrollmentDate;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}