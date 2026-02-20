package com.studentmanagement.controller;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@WebServlet("/api/students/*")
public class StudentServlet extends HttpServlet {
    private StudentDAO studentDAO;
    private Gson gson;

    @Override
    public void init() {
        studentDAO = new StudentDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Please login first")));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all students based on role
                List<Student> students;
                if ("admin".equals(role)) {
                    students = studentDAO.getAllStudents();
                } else {
                    students = studentDAO.getStudentsByUser(userId);
                }
                out.print(gson.toJson(students));
            } else {
                // Get single student
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length > 1) {
                    int id = Integer.parseInt(pathParts[1]);
                    Student student = studentDAO.getStudentById(id);

                    if (student != null) {
                        // Check if user has permission to view this student
                        if ("admin".equals(role) || student.getUserId() == userId) {
                            out.print(gson.toJson(student));
                        } else {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print(gson.toJson(Map.of("error", "You don't have permission to view this student")));
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(gson.toJson(Map.of("error", "Student not found")));
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Please login first")));
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try {
            // Read JSON from request
            BufferedReader reader = request.getReader();
            Student student = gson.fromJson(reader, Student.class);

            // Validate input
            if (student.getName() == null || student.getName().trim().isEmpty() ||
                    student.getEmail() == null || student.getEmail().trim().isEmpty() ||
                    student.getCourse() == null || student.getCourse().trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Name, email and course are required")));
                return;
            }

            // Set enrollment date if not provided
            if (student.getEnrollmentDate() == null) {
                student.setEnrollmentDate(new Date());
            }

            boolean success = studentDAO.addStudent(student, userId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of(
                        "message", "Student added successfully",
                        "success", true
                )));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Failed to add student. Email might already exist.")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Please login first")));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        try {
            BufferedReader reader = request.getReader();
            Student student = gson.fromJson(reader, Student.class);

            // Check if student exists
            Student existingStudent = studentDAO.getStudentById(student.getId());
            if (existingStudent == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Student not found")));
                return;
            }

            // Check permission
            if (!"admin".equals(role) && existingStudent.getUserId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "You don't have permission to update this student")));
                return;
            }

            boolean success = studentDAO.updateStudent(student);

            if (success) {
                out.print(gson.toJson(Map.of(
                        "message", "Student updated successfully",
                        "success", true
                )));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Failed to update student")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Please login first")));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.split("/").length > 1) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);

                // Check if student exists
                Student existingStudent = studentDAO.getStudentById(id);
                if (existingStudent == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Student not found")));
                    return;
                }

                // Check permission
                if (!"admin".equals(role) && existingStudent.getUserId() != userId) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(gson.toJson(Map.of("error", "You don't have permission to delete this student")));
                    return;
                }

                boolean success = studentDAO.deleteStudent(id);

                if (success) {
                    out.print(gson.toJson(Map.of(
                            "message", "Student deleted successfully",
                            "success", true
                    )));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "Failed to delete student")));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Student ID is required")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }
}