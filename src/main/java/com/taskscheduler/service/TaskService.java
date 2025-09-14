package com.taskscheduler.service;

import com.taskscheduler.entity.Task;
import com.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task addTask(Task task) {
        task.setDone(false);
        task.setId(null);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }


    public Optional<Task> updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(existingTask -> {
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setStartTime(taskDetails.getStartTime());
            existingTask.setEndTime(taskDetails.getEndTime());
            existingTask.setPriority(taskDetails.getPriority());
            existingTask.setCategory(taskDetails.getCategory());
            existingTask.setNotes(taskDetails.getNotes());

            if (taskDetails.getDone() != null) {
                existingTask.setDone(taskDetails.getDone());
            }
            return taskRepository.save(existingTask);
        });
    }


    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Task> toggleTaskStatus(Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setDone(!task.getDone());
            return taskRepository.save(task);
        });
    }

    public List<Task> getTasksByStatus(boolean done) {
        return taskRepository.findByDone(done);
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    public List<Task> getTasksByCategory(String category) {
        return taskRepository.findByCategory(category);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }

    public List<Task> getTodayTasks() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return taskRepository.findTasksBetween(startOfDay, endOfDay);
    }

    public List<Task> getTasksStartingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        return taskRepository.findTasksStartingSoon(now, oneHourLater);
    }

    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByDone(true);
        long pendingTasks = totalTasks - completedTasks;
        long highPriorityTasks = taskRepository.countByPriority("High");
        long workTasks = taskRepository.countByCategory("Work");
        long personalTasks = taskRepository.countByCategory("Personal");
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long todayTasks = taskRepository.countTasksBetween(startOfDay, endOfDay);
        long overdueTasks = taskRepository.countOverdueTasks(LocalDateTime.now());

        return new TaskStatistics(totalTasks, completedTasks, pendingTasks,
                highPriorityTasks, workTasks, personalTasks,
                todayTasks, overdueTasks);
    }

    public static class TaskStatistics {
        private final long totalTasks;
        private final long completedTasks;
        private final long pendingTasks;
        private final long highPriorityTasks;
        private final long workTasks;
        private final long personalTasks;
        private final long todayTasks;
        private final long overdueTasks;

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