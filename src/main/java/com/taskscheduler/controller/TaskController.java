package com.taskscheduler.controller;

import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody TaskDTO task) {
        try {
            TaskDTO savedTask = taskService.addTask(task);
            return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            Optional<TaskDTO> taskOptional = taskService.getTaskById(id);
            if (taskOptional.isPresent()) {
                return new ResponseEntity<>(taskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found with id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO task) {
        try {
            Optional<TaskDTO> updatedTaskOptional = taskService.updateTask(id, task);
            if (updatedTaskOptional.isPresent()) {
                return new ResponseEntity<>(updatedTaskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found with id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            if (deleted) {
                return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found with id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<?> toggleTaskStatus(@PathVariable Long id) {
        try {
            Optional<TaskDTO> updatedTaskOptional = taskService.toggleTaskStatus(id);

            if (updatedTaskOptional.isPresent()) {
                return new ResponseEntity<>(updatedTaskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found with id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{done}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable boolean done) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByStatus(done);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getTasksByPriority(@PathVariable String priority) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByPriority(priority);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by priority: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTasksByCategory(@PathVariable String category) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByCategory(category);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by category: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks() {
        try {
            List<TaskDTO> tasks = taskService.getOverdueTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching overdue tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTasks() {
        try {
            List<TaskDTO> tasks = taskService.getTodayTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching today's tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/starting-soon")
    public ResponseEntity<?> getTasksStartingSoon() {
        try {
            List<TaskDTO> tasks = taskService.getTasksStartingSoon();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching upcoming tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getTaskStatistics() {
        try {
            TaskService.TaskStatistics stats = taskService.getTaskStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching statistics: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}