package com.taskscheduler.service;

import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.entity.Task;
import com.taskscheduler.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Convert Entity to DTO
    private TaskDTO convertToDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getDescription(),
                task.getStartTime(),
                task.getEndTime(),
                task.getDone(),
                task.getPriority(),
                task.getCategory(),
                task.getNotes()
        );
    }

    // Convert DTO to Entity
    private Task convertToEntity(TaskDTO taskDTO) {
        Task task = new Task();
        task.setId(taskDTO.getId());
        task.setDescription(taskDTO.getDescription());
        task.setStartTime(taskDTO.getStartTime());
        task.setEndTime(taskDTO.getEndTime());
        task.setDone(taskDTO.getDone() != null ? taskDTO.getDone() : false);
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : "Medium");
        task.setCategory(taskDTO.getCategory() != null ? taskDTO.getCategory() : "Personal");
        task.setNotes(taskDTO.getNotes());
        return task;
    }

    // Add new task
    public TaskDTO addTask(TaskDTO taskDTO) {
        Task task = convertToEntity(taskDTO);
        task.setDone(false); // Ensure new tasks are not done by default
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    // Get all tasks
    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get task by ID
    public TaskDTO getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(this::convertToDTO).orElse(null);
    }

    // Update task
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Optional<Task> existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isPresent()) {
            Task existingTask = existingTaskOpt.get();

            // Update fields
            existingTask.setDescription(taskDTO.getDescription());
            existingTask.setStartTime(taskDTO.getStartTime());
            existingTask.setEndTime(taskDTO.getEndTime());
            existingTask.setPriority(taskDTO.getPriority());
            existingTask.setCategory(taskDTO.getCategory());
            existingTask.setNotes(taskDTO.getNotes());

            // Preserve the existing 'done' status unless explicitly provided
            if (taskDTO.getDone() != null) {
                existingTask.setDone(taskDTO.getDone());
            }

            Task updatedTask = taskRepository.save(existingTask);
            return convertToDTO(updatedTask);
        }
        return null;
    }

    // Delete task
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Toggle task completion status
    public TaskDTO toggleTaskStatus(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setDone(!task.getDone());
            Task updatedTask = taskRepository.save(task);
            return convertToDTO(updatedTask);
        }
        return null;
    }

    // Get tasks by completion status
    public List<TaskDTO> getTasksByStatus(boolean done) {
        List<Task> tasks = taskRepository.findByDone(done);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get tasks by priority
    public List<TaskDTO> getTasksByPriority(String priority) {
        List<Task> tasks = taskRepository.findByPriority(priority);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get tasks by category
    public List<TaskDTO> getTasksByCategory(String category) {
        List<Task> tasks = taskRepository.findByCategory(category);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get overdue tasks
    public List<TaskDTO> getOverdueTasks() {
        List<Task> tasks = taskRepository.findOverdueTasks(LocalDateTime.now());
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get today's tasks
    public List<TaskDTO> getTodayTasks() {
        List<Task> tasks = taskRepository.findTodayTasks(LocalDateTime.now());
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get tasks starting soon (next hour)
    public List<TaskDTO> getTasksStartingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        List<Task> tasks = taskRepository.findTasksStartingSoon(now, oneHourLater);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get task statistics
    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByDone(true);
        long pendingTasks = taskRepository.countByDone(false);
        long highPriorityTasks = taskRepository.countByPriority("High");
        long workTasks = taskRepository.countByCategory("Work");
        long personalTasks = taskRepository.countByCategory("Personal");

        List<Task> todayTasks = taskRepository.findTodayTasks(LocalDateTime.now());
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDateTime.now());

        return new TaskStatistics(totalTasks, completedTasks, pendingTasks,
                highPriorityTasks, workTasks, personalTasks,
                todayTasks.size(), overdueTasks.size());
    }

    // Inner class for statistics
    public static class TaskStatistics {
        private long totalTasks;
        private long completedTasks;
        private long pendingTasks;
        private long highPriorityTasks;
        private long workTasks;
        private long personalTasks;
        private long todayTasks;
        private long overdueTasks;

        public TaskStatistics(long totalTasks, long completedTasks, long pendingTasks,
                              long highPriorityTasks, long workTasks, long personalTasks,
                              long todayTasks, long overdueTasks) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.highPriorityTasks = highPriorityTasks;
            this.workTasks = workTasks;
            this.personalTasks = personalTasks;
            this.todayTasks = todayTasks;
            this.overdueTasks = overdueTasks;
        }

        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getPendingTasks() { return pendingTasks; }
        public long getHighPriorityTasks() { return highPriorityTasks; }
        public long getWorkTasks() { return workTasks; }
        public long getPersonalTasks() { return personalTasks; }
        public long getTodayTasks() { return todayTasks; }
        public long getOverdueTasks() { return overdueTasks; }
    }
}
