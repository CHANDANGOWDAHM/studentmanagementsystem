// Get context path dynamically
function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}

const contextPath = getContextPath();
const API_BASE = contextPath + '/api';

// Check authentication status
async function checkAuth() {
    try {
        const response = await fetch(`${API_BASE}/auth/check`);
        const data = await response.json();

        if (!data.authenticated) {
            const currentPage = window.location.pathname;
            if (!currentPage.includes('login.html') && !currentPage.includes('register.html')) {
                window.location.href = contextPath + '/login.html';
            }
            return false;
        }

        updateUserInfo(data);
        return true;

    } catch (error) {
        console.error('Auth check failed:', error);
        return false;
    }
}

// Update UI with user information
function updateUserInfo(userData) {
    const userDisplay = document.getElementById('userDisplay');
    if (userDisplay) {
        userDisplay.innerHTML = `
            <div class="user-info">
                <span class="user-name">${userData.fullName || userData.username}</span>
                ${userData.role === 'admin' ? '<span class="user-role">Admin</span>' : ''}
            </div>
        `;
    }
}

// Register function
async function register(event) {
    event.preventDefault();

    const fullName = document.getElementById('fullName').value;
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        showAlert('Passwords do not match', 'error');
        return;
    }

    if (password.length < 6) {
        showAlert('Password must be at least 6 characters', 'error');
        return;
    }

    if (username.length < 3) {
        showAlert('Username must be at least 3 characters', 'error');
        return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Please enter a valid email address', 'error');
        return;
    }

    const registerData = {
        fullName: fullName,
        username: username,
        email: email,
        password: password
    };

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            showAlert('Registration successful! Redirecting to login...', 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/login.html';
            }, 2000);
        } else {
            showAlert(data.error || 'Registration failed', 'error');
        }

    } catch (error) {
        console.error('Registration error:', error);
        showAlert('Registration failed. Please try again.', 'error');
    }
}

// Login function
async function login(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showAlert('Please enter username and password', 'error');
        return;
    }

    const loginData = {
        username: username,
        password: password
    };

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            showAlert('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/index.html';
            }, 1500);
        } else {
            showAlert(data.error || 'Login failed', 'error');
        }

    } catch (error) {
        console.error('Login error:', error);
        showAlert('Login failed. Please try again.', 'error');
    }
}

// Logout function
async function logout() {
    try {
        const response = await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (data.success) {
            window.location.href = contextPath + '/login.html';
        }

    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = contextPath + '/login.html';
    }
}

// Load students for view-students page
async function loadStudents() {
    if (!await checkAuth()) return;

    try {
        const response = await fetch(`${API_BASE}/students`);

        if (response.status === 401) {
            window.location.href = contextPath + '/login.html';
            return;
        }

        const students = await response.json();

        const tableBody = document.getElementById('studentsTableBody');
        if (!tableBody) return;

        if (students.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="empty-state">No students found. Click "Add Student" to create one.</td></tr>';
            return;
        }

        tableBody.innerHTML = '';

        students.forEach(student => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${student.id}</td>
                <td>${escapeHtml(student.name)}</td>
                <td>${escapeHtml(student.email)}</td>
                <td><span class="course-badge">${escapeHtml(student.course)}</span></td>
                <td>${student.phone ? escapeHtml(student.phone) : '-'}</td>
                <td>${new Date(student.enrollmentDate).toLocaleDateString()}</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn-edit" onclick="editStudent(${student.id})">
                            <i class="fas fa-edit"></i> Edit
                        </button>
                        <button class="btn-delete" onclick="deleteStudent(${student.id})">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </div>
                </td>
            `;
            tableBody.appendChild(row);
        });

    } catch (error) {
        console.error('Error loading students:', error);
        showAlert('Error loading students', 'error');
    }
}

// Helper function to escape HTML
function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Add student
async function addStudent(event) {
    event.preventDefault();

    if (!await checkAuth()) return;

    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const course = document.getElementById('course').value;
    const phone = document.getElementById('phone').value;
    const address = document.getElementById('address').value;

    if (!name || !email || !course) {
        showAlert('Name, email and course are required', 'error');
        return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Please enter a valid email address', 'error');
        return;
    }

    const formData = {
        name: name,
        email: email,
        course: course,
        phone: phone,
        address: address,
        enrollmentDate: new Date().toISOString()
    };

    try {
        const response = await fetch(`${API_BASE}/students`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            showAlert('Student added successfully!', 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/view-students.html';
            }, 2000);
        } else {
            showAlert(data.error || 'Error adding student', 'error');
        }

    } catch (error) {
        console.error('Error adding student:', error);
        showAlert('Error adding student', 'error');
    }
}

// Edit student
function editStudent(id) {
    window.location.href = contextPath + `/update-student.html?id=${id}`;
}

// Load student data for update page
async function loadStudentForUpdate() {
    if (!await checkAuth()) return;

    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');

    if (!id) {
        window.location.href = contextPath + '/view-students.html';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/students/${id}`);

        if (response.status === 401) {
            window.location.href = contextPath + '/login.html';
            return;
        }

        if (response.status === 403) {
            showAlert('You don\'t have permission to edit this student', 'error');
            setTimeout(() => {
                window.location.href = contextPath + '/view-students.html';
            }, 2000);
            return;
        }

        const student = await response.json();

        document.getElementById('studentId').value = student.id;
        document.getElementById('name').value = student.name;
        document.getElementById('email').value = student.email;
        document.getElementById('course').value = student.course;
        document.getElementById('phone').value = student.phone || '';
        document.getElementById('address').value = student.address || '';

    } catch (error) {
        console.error('Error loading student:', error);
        showAlert('Error loading student data', 'error');
    }
}

// Update student
async function updateStudent(event) {
    event.preventDefault();

    if (!await checkAuth()) return;

    const id = parseInt(document.getElementById('studentId').value);
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const course = document.getElementById('course').value;
    const phone = document.getElementById('phone').value;
    const address = document.getElementById('address').value;

    if (!name || !email || !course) {
        showAlert('Name, email and course are required', 'error');
        return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Please enter a valid email address', 'error');
        return;
    }

    const formData = {
        id: id,
        name: name,
        email: email,
        course: course,
        phone: phone,
        address: address,
        enrollmentDate: new Date().toISOString()
    };

    try {
        const response = await fetch(`${API_BASE}/students`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            showAlert('Student updated successfully!', 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/view-students.html';
            }, 2000);
        } else {
            showAlert(data.error || 'Error updating student', 'error');
        }

    } catch (error) {
        console.error('Error updating student:', error);
        showAlert('Error updating student', 'error');
    }
}

// Delete student
async function deleteStudent(id) {
    if (!await checkAuth()) return;

    if (confirm('Are you sure you want to delete this student?')) {
        try {
            const response = await fetch(`${API_BASE}/students/${id}`, {
                method: 'DELETE'
            });

            const data = await response.json();

            if (response.ok && data.success) {
                showAlert('Student deleted successfully!', 'success');
                loadStudents();
            } else {
                showAlert(data.error || 'Error deleting student', 'error');
            }

        } catch (error) {
            console.error('Error deleting student:', error);
            showAlert('Error deleting student', 'error');
        }
    }
}

// Show alert message
function showAlert(message, type) {
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `
        <div class="alert-content">
            <span class="alert-message">${message}</span>
            <button class="alert-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
        </div>
    `;

    document.body.appendChild(alertDiv);

    setTimeout(() => {
        alertDiv.classList.add('alert-fade-out');
        setTimeout(() => alertDiv.remove(), 500);
    }, 3000);
}

// Initialize based on page
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('login.html')) {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', login);
        }
    }
    else if (window.location.pathname.includes('register.html')) {
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', register);
        }
    }
    else {
        checkAuth().then(isAuthenticated => {
            if (isAuthenticated) {
                if (window.location.pathname.includes('view-students.html')) {
                    loadStudents();
                }

                if (window.location.pathname.includes('update-student.html')) {
                    loadStudentForUpdate();
                }

                // Load dashboard stats
                if (window.location.pathname.includes('index.html') || window.location.pathname === contextPath + '/') {
                    loadDashboardStats();
                }
            }
        });

        const addForm = document.getElementById('addStudentForm');
        if (addForm) {
            addForm.addEventListener('submit', addStudent);
        }

        const updateForm = document.getElementById('updateStudentForm');
        if (updateForm) {
            updateForm.addEventListener('submit', updateStudent);
        }
    }
});

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE}/students`);
        const students = await response.json();
        document.getElementById('totalStudents').textContent = students.length;
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}