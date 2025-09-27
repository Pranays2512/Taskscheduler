-- Updated database schema with user authentication
DROP DATABASE IF EXISTS task_scheduler;
CREATE DATABASE task_scheduler;
USE task_scheduler;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tasks table with user relationship
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    priority VARCHAR(50) DEFAULT 'Medium',
    category VARCHAR(100) DEFAULT 'Personal',
    notes TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_done ON tasks(done);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_category ON tasks(category);
CREATE INDEX idx_tasks_start_time ON tasks(start_time);
CREATE INDEX idx_tasks_end_time ON tasks(end_time);
CREATE INDEX idx_users_email ON users(email);

-- Sample data (passwords are hashed for 'password123')
-- You would need to hash these passwords using BCrypt in your application
INSERT INTO users (name, email, password) VALUES
('John Doe', 'john@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
('Jane Smith', 'jane@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Sample tasks for the users
INSERT INTO tasks (description, start_time, end_time, priority, category, notes, done, user_id) VALUES
('Complete Spring Boot project', '2024-12-07 09:00:00', '2024-12-07 17:00:00', 'High', 'Work', 'Implement JWT authentication', FALSE, 1),
('Grocery shopping', '2024-12-07 18:00:00', '2024-12-07 19:30:00', 'Medium', 'Personal', 'Buy vegetables, milk, and bread', FALSE, 1),
('Morning workout', '2024-12-08 07:00:00', '2024-12-08 08:00:00', 'High', 'Health', '30 minutes cardio + weights', FALSE, 1),
('Team meeting', '2024-12-08 10:00:00', '2024-12-08 11:00:00', 'High', 'Work', 'Discuss project progress', FALSE, 2),
('Read book', '2024-12-08 20:00:00', '2024-12-08 21:00:00', 'Low', 'Personal', 'Continue reading "Clean Code"', FALSE, 2);