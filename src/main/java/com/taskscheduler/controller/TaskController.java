//src/main/java/com/taskscheduler/controller/TaskController.java
package com.taskscheduler.controller;

import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
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

    private Long getUserIdFromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userId;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody TaskDTO task, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            TaskDTO savedTask = taskService.addTask(task, userId);
            return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTasks(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getAllTasksByUser(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<TaskDTO> taskOptional = taskService.getTaskByIdAndUser(id, userId);
            if (taskOptional.isPresent()) {
                return new ResponseEntity<>(taskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found or access denied", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO task, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<TaskDTO> updatedTaskOptional = taskService.updateTask(id, task, userId);
            if (updatedTaskOptional.isPresent()) {
                return new ResponseEntity<>(updatedTaskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found or access denied", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            boolean deleted = taskService.deleteTask(id, userId);
            if (deleted) {
                return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found or access denied", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<?> toggleTaskStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<TaskDTO> updatedTaskOptional = taskService.toggleTaskStatus(id, userId);

            if (updatedTaskOptional.isPresent()) {
                return new ResponseEntity<>(updatedTaskOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Task not found or access denied", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{done}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable boolean done, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getTasksByStatus(done, userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getTasksByPriority(@PathVariable String priority, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getTasksByPriority(priority, userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by priority: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTasksByCategory(@PathVariable String category, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getTasksByCategory(category, userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks by category: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getOverdueTasks(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching overdue tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTasks(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getTodayTasks(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching today's tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/starting-soon")
    public ResponseEntity<?> getTasksStartingSoon(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TaskDTO> tasks = taskService.getTasksStartingSoon(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching upcoming tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getTaskStatistics(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            TaskService.TaskStatistics stats = taskService.getTaskStatistics(userId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching statistics: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}