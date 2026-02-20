package com.studentmanagement.controller;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.model.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() {
        userDAO = new UserDAO();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            if (pathInfo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Invalid request")));
                return;
            }

            switch (pathInfo) {
                case "/register":
                    handleRegister(request, response, out);
                    break;
                case "/login":
                    handleLogin(request, response, out);
                    break;
                case "/logout":
                    handleLogout(request, response, out);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Endpoint not found")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            if ("/check".equals(pathInfo)) {
                checkAuth(request, response, out);
            } else if ("/profile".equals(pathInfo)) {
                getProfile(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Endpoint not found")));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException {

        BufferedReader reader = request.getReader();
        Map<String, String> credentials = gson.fromJson(reader, Map.class);

        String username = credentials.get("username");
        String email = credentials.get("email");
        String password = credentials.get("password");
        String fullName = credentials.get("fullName");

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                fullName == null || fullName.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "All fields are required")));
            return;
        }

        // Validate username length
        if (username.length() < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Username must be at least 3 characters")));
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Password must be at least 6 characters")));
            return;
        }

        // Check if username exists
        if (userDAO.isUsernameExists(username)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson(Map.of("error", "Username already exists")));
            return;
        }

        // Check if email exists
        if (userDAO.isEmailExists(email)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson(Map.of("error", "Email already registered")));
            return;
        }

        // Create new user
        User user = new User(username, email, password, fullName, "user");
        boolean registered = userDAO.registerUser(user);

        if (registered) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(Map.of(
                    "message", "Registration successful! Please login.",
                    "success", true
            )));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Registration failed. Please try again.")));
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException {

        BufferedReader reader = request.getReader();
        Map<String, String> credentials = gson.fromJson(reader, Map.class);

        String username = credentials.get("username");
        String password = credentials.get("password");

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Username and password are required")));
            return;
        }

        // Attempt login
        User user = userDAO.loginUser(username, password);

        if (user != null) {
            // Create session
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Create response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Login successful");
            responseData.put("success", true);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());

            responseData.put("user", userData);

            out.print(gson.toJson(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Invalid username or password")));
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        out.print(gson.toJson(Map.of(
                "message", "Logout successful",
                "success", true
        )));
    }

    private void checkAuth(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("authenticated", true);
            userData.put("userId", session.getAttribute("userId"));
            userData.put("username", session.getAttribute("username"));
            userData.put("fullName", session.getAttribute("fullName"));
            userData.put("role", session.getAttribute("role"));

            out.print(gson.toJson(userData));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of(
                    "authenticated", false,
                    "error", "Not authenticated"
            )));
        }
    }

    private void getProfile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            User user = userDAO.getUserById(userId);

            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("email", user.getEmail());
                userData.put("fullName", user.getFullName());
                userData.put("role", user.getRole());
                userData.put("createdAt", user.getCreatedAt());

                out.print(gson.toJson(userData));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "User not found")));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("error", "Not authenticated")));
        }
    }
}