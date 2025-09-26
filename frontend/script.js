const API_BASE = 'http://localhost:8080/api/tasks';
let tasks = [];
let notificationTimeouts = [];

function showTab(tabName) {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(t => t.classList.remove('active'));
    contents.forEach(c => c.classList.remove('active'));

    document.querySelector(`[onclick="showTab('${tabName}')"]`).classList.add('active');
    document.getElementById(tabName).classList.add('active');

    if (tabName === 'view') {
        loadTasks();
    } else if (tabName === 'stats') {
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
        const response = await fetch(`${API_BASE}/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });

        if (response.ok) {
            showNotification('Task added successfully!');
            resetForm();
            showTab('view');
        } else {
            const errorData = await response.json();
            showNotification(`Failed to add task: ${errorData.message || 'Unknown error'}`, 'alert');
        }
    } catch (error) {
        showNotification('Error connecting to server', 'alert');
    }
});

document.getElementById('editTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();

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
        const response = await fetch(`${API_BASE}/edit/${taskId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });

        if (response.ok) {
            showNotification('Task updated successfully!');
            closeEditModal();
            loadTasks();
        } else {
            const errorData = await response.json();
            showNotification(`Failed to update task: ${errorData.message || 'Unknown error'}`, 'alert');
        }
    } catch (error) {
        showNotification('Error connecting to server', 'alert');
    }
});

function resetForm() {
    document.getElementById('taskForm').reset();
}

async function loadTasks() {
    try {
        const response = await fetch(`${API_BASE}/all`);
        if (response.ok) {
            tasks = await response.json();
            displayTasks(tasks);
            updateStatistics();
        } else {
            showNotification('Failed to load tasks', 'alert');
        }
    } catch (error) {
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

// ✨ FIX: This is now the one and only version of this function.
function displaySearchResults(searchResults) {
    displayTasks(searchResults, 'searchResults');
}

async function toggleTaskStatus(taskId) {
    try {
        const response = await fetch(`${API_BASE}/toggle/${taskId}`, { method: 'PUT' });
        if (response.ok) {
            showNotification('Task status updated!');
            loadTasks();
        } else {
            showNotification('Failed to update task status', 'alert');
        }
    } catch (error) {
        showNotification('Error connecting to server', 'alert');
    }
}

async function deleteTask(taskId) {
    if (confirm('Are you sure you want to delete this task?')) {
        try {
            const response = await fetch(`${API_BASE}/delete/${taskId}`, { method: 'DELETE' });
            if (response.ok) {
                showNotification('Task deleted successfully!');
                loadTasks();
            } else {
                showNotification('Failed to delete task', 'alert');
            }
        } catch (error) {
            showNotification('Error connecting to server', 'alert');
        }
    }
}

function editTask(taskId) {
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

// ✨ FIX: Improved search function for better UX.
async function searchTask() {
    const taskId = document.getElementById('searchId').value;
    if (!taskId) {
        showNotification('Please enter a task ID', 'alert');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/get/${taskId}`);
        if (response.ok) {
            const task = await response.json();
            displaySearchResults([task]); // Display the found task
        } else if (response.status === 404) {
            // The displayTasks function will show the "No tasks found" message,
            // so the notification is redundant.
            displaySearchResults([]);
        } else {
            showNotification('Failed to search for task', 'alert');
        }
    } catch (error) {
        showNotification('Error connecting to server', 'alert');
    }
}



function updateStatistics() {
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
    showTab('view');
    const now = new Date();
    const startTime = new Date(now.getTime() + 60000);
    const endTime = new Date(now.getTime() + 3600000);

    document.getElementById('startTime').value = startTime.toISOString().slice(0, 16);
    document.getElementById('endTime').value = endTime.toISOString().slice(0, 16);
});

document.getElementById('editModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeEditModal();
    }
});