CREATE DATABASE IF NOT EXISTS task_scheduler;
USE task_scheduler;

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    priority VARCHAR(50) DEFAULT 'Medium',
    category VARCHAR(100) DEFAULT 'Personal',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO tasks (description, start_time, end_time, priority, category, notes, done) VALUES
('Complete Spring Boot project', '2024-12-07 09:00:00', '2024-12-07 17:00:00', 'High', 'Work', 'Implement all CRUD operations', FALSE),
('Grocery shopping', '2024-12-07 18:00:00', '2024-12-07 19:30:00', 'Medium', 'Personal', 'Buy vegetables, milk, and bread', FALSE),
('Morning workout', '2024-12-08 07:00:00', '2024-12-08 08:00:00', 'High', 'Health', '30 minutes cardio + weights', FALSE),
('Team meeting', '2024-12-08 10:00:00', '2024-12-08 11:00:00', 'High', 'Work', 'Discuss project progress', FALSE),
('Read book', '2024-12-08 20:00:00', '2024-12-08 21:00:00', 'Low', 'Personal', 'Continue reading "Clean Code"', FALSE);
