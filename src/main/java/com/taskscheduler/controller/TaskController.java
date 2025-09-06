package com.taskscheduler.controller;

import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Add new task
    @PostMapping("/add")
    public ResponseEntity<TaskDTO> addTask(@RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO savedTask = taskService.addTask(taskDTO);
            return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all tasks
    @GetMapping("/all")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get task by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            if (task != null) {
                return new ResponseEntity<>(task, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update task
    @PutMapping("/edit/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            if (updatedTask != null) {
                return new ResponseEntity<>(updatedTask, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete task
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            if (deleted) {
                return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting task", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Toggle task completion status
    @PutMapping("/toggle/{id}")
    public ResponseEntity<TaskDTO> toggleTaskStatus(@PathVariable Long id) {
        try {
            TaskDTO updatedTask = taskService.toggleTaskStatus(id);
            if (updatedTask != null) {
                return new ResponseEntity<>(updatedTask, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get tasks by status
    @GetMapping("/status/{done}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(@PathVariable boolean done) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByStatus(done);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get tasks by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskDTO>> getTasksByPriority(@PathVariable String priority) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByPriority(priority);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get tasks by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TaskDTO>> getTasksByCategory(@PathVariable String category) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByCategory(category);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get overdue tasks
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDTO>> getOverdueTasks() {
        try {
            List<TaskDTO> tasks = taskService.getOverdueTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get today's tasks
    @GetMapping("/today")
    public ResponseEntity<List<TaskDTO>> getTodayTasks() {
        try {
            List<TaskDTO> tasks = taskService.getTodayTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get tasks starting soon
    @GetMapping("/starting-soon")
    public ResponseEntity<List<TaskDTO>> getTasksStartingSoon() {
        try {
            List<TaskDTO> tasks = taskService.getTasksStartingSoon();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get task statistics
    @GetMapping("/statistics")
    public ResponseEntity<TaskService.TaskStatistics> getTaskStatistics() {
        try {
            TaskService.TaskStatistics stats = taskService.getTaskStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}