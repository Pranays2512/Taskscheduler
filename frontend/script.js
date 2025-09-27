const API_BASE = 'http://localhost:8080/api';
let tasks = [];
let notificationTimeouts = [];
let isAuthenticated = false;
let currentUser = null;
let authToken = null;

// Authentication functions
function checkAuthStatus() {
    const storedUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null');
    const token = sessionStorage.getItem('authToken');

    if (storedUser && token) {
        authToken = token;
        currentUser = storedUser;

        // Validate token with server
        validateTokenWithServer(token).then(isValid => {
            if (isValid) {
                isAuthenticated = true;
                showMainApp();
            } else {
                clearAuthData();
                showWelcomeScreen();
            }
        }).catch(() => {
            clearAuthData();
            showWelcomeScreen();
        });
    } else {
        isAuthenticated = false;
        currentUser = null;
        authToken = null;
        showWelcomeScreen();
    }
}

async function validateTokenWithServer(token) {
    try {
        const response = await fetch(`${API_BASE}/auth/validate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.ok;
    } catch (error) {
        console.error('Token validation failed:', error);
        return false;
    }
}

function clearAuthData() {
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('authToken');
    isAuthenticated = false;
    currentUser = null;
    authToken = null;
}

function showAuthModal() {
    document.getElementById('authModal').style.display = 'flex';
}

function hideAuthModal() {
    document.getElementById('authModal').style.display = 'none';
}

function switchAuthTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.auth-tab').forEach(tab => tab.classList.remove('active'));
    document.querySelector(`[onclick="switchAuthTab('${tabName}')"]`).classList.add('active');

    // Update forms
    document.querySelectorAll('.auth-form').forEach(form => form.classList.remove('active'));
    document.getElementById(tabName + 'Form').classList.add('active');
}

function showMainApp() {
    document.getElementById('welcomeScreen').style.display = 'none';
    document.getElementById('mainContent').style.display = 'block';
    document.getElementById('authHeader').style.display = 'flex';
    document.getElementById('userName').textContent = currentUser?.name || currentUser?.email || 'User';
    hideAuthModal();
}

function showWelcomeScreen() {
    document.getElementById('welcomeScreen').style.display = 'block';
    document.getElementById('mainContent').style.display = 'none';
    document.getElementById('authHeader').style.display = 'none';
    showAuthModal();
}

function signOut() {
    if (confirm('Are you sure you want to sign out?')) {
        clearAuthData();
        showWelcomeScreen();
        showNotification('Signed out successfully!');
    }
}

// Real authentication handlers
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password')
    };

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        if (response.ok) {
            const authResponse = await response.json();

            // Store auth data
            sessionStorage.setItem('currentUser', JSON.stringify({
                id: authResponse.id,
                name: authResponse.name,
                email: authResponse.email
            }));
            sessionStorage.setItem('authToken', authResponse.token);

            currentUser = {
                id: authResponse.id,
                name: authResponse.name,
                email: authResponse.email
            };
            authToken = authResponse.token;
            isAuthenticated = true;

            showNotification('Signed in successfully!');
            showMainApp();
            document.getElementById('loginForm').reset();
        } else {
            const errorText = await response.text();
            showNotification(errorText || 'Sign in failed', 'alert');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Network error. Please try again.', 'alert');
    }
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const registerData = {
        name: formData.get('name'),
        email: formData.get('email'),
        password: formData.get('password'),
        confirmPassword: formData.get('confirmPassword')
    };

    if (registerData.password !== registerData.confirmPassword) {
        showNotification('Passwords do not match!', 'alert');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        });

        if (response.ok) {
            const authResponse = await response.json();

            // Store auth data
            sessionStorage.setItem('currentUser', JSON.stringify({
                id: authResponse.id,
                name: authResponse.name,
                email: authResponse.email
            }));
            sessionStorage.setItem('authToken', authResponse.token);

            currentUser = {
                id: authResponse.id,
                name: authResponse.name,
                email: authResponse.email
            };
            authToken = authResponse.token;
            isAuthenticated = true;

            showNotification('Account created successfully!');
            showMainApp();
            document.getElementById('registerForm').reset();
        } else {
            const errorText = await response.text();
            showNotification(errorText || 'Registration failed', 'alert');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification('Network error. Please try again.', 'alert');
    }
});

// Auth guard for API calls
function makeAuthenticatedRequest(url, options = {}) {
    if (!isAuthenticated || !authToken) {
        showNotification('Please sign in to continue', 'alert');
        showWelcomeScreen();
        return Promise.reject(new Error('Not authenticated'));
    }

    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`,
            ...options.headers
        }
    };

    return fetch(url, { ...options, ...defaultOptions });
}

function showTab(tabName) {
    if (!isAuthenticated) {
        showNotification('Please sign in to access tasks', 'alert');
        return;
    }

    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(t => t.classList.remove('active'));
    contents.forEach(c => c.classList.remove('active'));

    document.querySelector(`[onclick="showTab('${tabName}')"]`).classList.add('active');
    document.getElementById(tabName).classList.add('active');

    if (tabName === 'view') {
        loadTasks();
    } else if (tabName === 'stats') {
        updateStatistics();
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <strong>${type === 'alert' ? '⚠️ Alert' : 'ℹ️ Info'}</strong><br>
        ${message}
    `;

    document.getElementById('notificationArea').appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 5000);
}

function checkTaskNotifications() {
    if (!isAuthenticated) return;

    const now = new Date();
    tasks.forEach(task => {
        if (!task.done) {
            const startTime = new Date(task.startTime);
            const endTime = new Date(task.endTime);

            if (Math.abs(startTime - now) <= 60000 && !task.startNotified) {
                showNotification(`Task "${task.description}" is starting now!`, 'alert');
                task.startNotified = true;
            }

            if (Math.abs(endTime - now) <= 60000 && !task.endNotified) {
                showNotification(`Task "${task.description}" is ending now!`, 'alert');
                task.endNotified = true;
            }
        }
    });
}

setInterval(checkTaskNotifications, 30000);

document.getElementById('taskForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!isAuthenticated) {
        showNotification('Please sign in to add tasks', 'alert');
        return;
    }

    const formData = new FormData(e.target);
    const taskData = {
        description: formData.get('description'),
        startTime: formData.get('startTime'),
        endTime: formData.get('endTime'),
        priority: formData.get('priority'),
        category: formData.get('category'),
        notes: formData.get('notes') || '',
        done: false
    };

    try {
        const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/add`, {
            method: 'POST',
            body: JSON.stringify(taskData)
        });

        if (response.ok) {
            showNotification('Task added successfully!');
            resetForm();
            showTab('view');
        } else {
            const errorData = await response.text();
            showNotification(`Failed to add task: ${errorData}`, 'alert');
        }
    } catch (error) {
        if (error.message === 'Not authenticated') return;
        showNotification('Error connecting to server', 'alert');
    }
});

document.getElementById('editTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!isAuthenticated) {
        showNotification('Please sign in to edit tasks', 'alert');
        return;
    }

    const formData = new FormData(e.target);
    const taskId = document.getElementById('editTaskId').value;
    const taskData = {
        id: taskId,
        description: formData.get('description'),
        startTime: formData.get('startTime'),
        endTime: formData.get('endTime'),
        priority: formData.get('priority'),
        category: formData.get('category'),
        notes: formData.get('notes') || ''
    };

    try {
        const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/edit/${taskId}`, {
            method: 'PUT',
            body: JSON.stringify(taskData)
        });

        if (response.ok) {
            showNotification('Task updated successfully!');
            closeEditModal();
            loadTasks();
        } else {
            const errorData = await response.text();
            showNotification(`Failed to update task: ${errorData}`, 'alert');
        }
    } catch (error) {
        if (error.message === 'Not authenticated') return;
        showNotification('Error connecting to server', 'alert');
    }
});

function resetForm() {
    document.getElementById('taskForm').reset();
}

async function loadTasks() {
    if (!isAuthenticated) {
        showNotification('Please sign in to view tasks', 'alert');
        return;
    }

    try {
        const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/all`);
        if (response.ok) {
            tasks = await response.json();
            displayTasks(tasks);
            updateStatistics();
        } else {
            showNotification('Failed to load tasks', 'alert');
        }
    } catch (error) {
        if (error.message === 'Not authenticated') return;
        showNotification('Error connecting to server', 'alert');
    }
}

function displayTasks(tasksToShow, containerId = 'tasksContainer') {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    if (tasksToShow.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #718096; font-size: 18px; padding: 40px;">No tasks found</p>';
        return;
    }

    tasksToShow.forEach(task => {
        const taskCard = document.createElement('div');
        taskCard.className = `task-card ${task.priority.toLowerCase()}`;
        taskCard.innerHTML = `
            <div class="task-header">
                <div class="task-title">${task.description}</div>
                <div class="task-id">ID: ${task.id}</div>
                <div class="task-status ${task.done ? 'done' : 'pending'}">
                    ${task.done ? 'Completed' : 'Pending'}
                </div>
            </div>
            <div class="task-details">
                <div class="task-detail"><strong>Start:</strong> ${new Date(task.startTime).toLocaleString()}</div>
                <div class="task-detail"><strong>End:</strong> ${new Date(task.endTime).toLocaleString()}</div>
                <div class="task-detail"><strong>Priority:</strong> <span class="priority-${task.priority.toLowerCase()}">${task.priority}</span></div>
                <div class="task-detail"><strong>Category:</strong> ${task.category}</div>
            </div>
            ${task.notes ? `<div class="task-detail" style="margin-top: 10px;"><strong>Notes:</strong> ${task.notes}</div>` : ''}
            <div class="task-actions">
                <button class="btn btn-secondary" onclick="editTask(${task.id})">✏️ Edit</button>
                <button class="btn ${task.done ? 'btn-secondary' : 'btn-success'}" onclick="toggleTaskStatus(${task.id})">
                    ${task.done ? '↶ Mark Undone' : '✓ Mark Done'}
                </button>
                <button class="btn btn-danger" onclick="deleteTask(${task.id})">🗑️ Delete</button>
            </div>
        `;
        container.appendChild(taskCard);
    });
}

function displaySearchResults(searchResults) {
    displayTasks(searchResults, 'searchResults');
}

async function toggleTaskStatus(taskId) {
    if (!isAuthenticated) {
        showNotification('Please sign in to update tasks', 'alert');
        return;
    }

    try {
        const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/toggle/${taskId}`, { method: 'PUT' });
        if (response.ok) {
            showNotification('Task status updated!');
            loadTasks();
        } else {
            showNotification('Failed to update task status', 'alert');
        }
    } catch (error) {
        if (error.message === 'Not authenticated') return;
        showNotification('Error connecting to server', 'alert');
    }
}

async function deleteTask(taskId) {
    if (!isAuthenticated) {
        showNotification('Please sign in to delete tasks', 'alert');
        return;
    }

    if (confirm('Are you sure you want to delete this task?')) {
        try {
            const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/delete/${taskId}`, { method: 'DELETE' });
            if (response.ok) {
                showNotification('Task deleted successfully!');
                loadTasks();
            } else {
                showNotification('Failed to delete task', 'alert');
            }
        } catch (error) {
            if (error.message === 'Not authenticated') return;
            showNotification('Error connecting to server', 'alert');
        }
    }
}

function editTask(taskId) {
    if (!isAuthenticated) {
        showNotification('Please sign in to edit tasks', 'alert');
        return;
    }

    const task = tasks.find(t => t.id === taskId);
    if (!task) return;

    document.getElementById('editTaskId').value = task.id;
    document.getElementById('editDescription').value = task.description;
    document.getElementById('editCategory').value = task.category;
    document.getElementById('editStartTime').value = task.startTime.slice(0, 16);
    document.getElementById('editEndTime').value = task.endTime.slice(0, 16);
    document.getElementById('editPriority').value = task.priority;
    document.getElementById('editNotes').value = task.notes || '';

    document.getElementById('editModal').style.display = 'block';
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

async function searchTask() {
    if (!isAuthenticated) {
        showNotification('Please sign in to search tasks', 'alert');
        return;
    }

    const taskId = document.getElementById('searchId').value;
    if (!taskId) {
        showNotification('Please enter a task ID', 'alert');
        return;
    }

    try {
        const response = await makeAuthenticatedRequest(`${API_BASE}/tasks/get/${taskId}`);
        if (response.ok) {
            const task = await response.json();
            displaySearchResults([task]);
        } else if (response.status === 404) {
            displaySearchResults([]);
        } else {
            showNotification('Failed to search for task', 'alert');
        }
    } catch (error) {
        if (error.message === 'Not authenticated') return;
        showNotification('Error connecting to server', 'alert');
    }
}

function updateStatistics() {
    if (!isAuthenticated) return;

    const total = tasks.length;
    const completed = tasks.filter(t => t.done).length;
    const pending = total - completed;
    const highPriority = tasks.filter(t => t.priority === 'High').length;
    const work = tasks.filter(t => t.category === 'Work').length;
    const personal = tasks.filter(t => t.category === 'Personal').length;

    const today = new Date().toDateString();
    const todayTasks = tasks.filter(t => new Date(t.startTime).toDateString() === today).length;

    const now = new Date();
    const overdue = tasks.filter(t => !t.done && new Date(t.endTime) < now).length;

    document.getElementById('totalTasks').textContent = total;
    document.getElementById('completedTasks').textContent = completed;
    document.getElementById('pendingTasks').textContent = pending;
    document.getElementById('highPriorityTasks').textContent = highPriority;
    document.getElementById('workTasks').textContent = work;
    document.getElementById('personalTasks').textContent = personal;
    document.getElementById('todayTasks').textContent = todayTasks;
    document.getElementById('overdueTasks').textContent = overdue;
}

document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();

    if (isAuthenticated) {
        const now = new Date();
        const startTime = new Date(now.getTime() + 60000);
        const endTime = new Date(now.getTime() + 3600000);

        document.getElementById('startTime').value = startTime.toISOString().slice(0, 16);
        document.getElementById('endTime').value = endTime.toISOString().slice(0, 16);
    }
});

document.getElementById('editModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeEditModal();
    }
});

document.getElementById('authModal').addEventListener('click', function(e) {
    if (e.target === this && currentUser) {
        hideAuthModal();
    }
});