package com.taskscheduler.service;

import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.entity.Task;
import com.taskscheduler.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


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


    private List<TaskDTO> convertToDTOList(List<Task> tasks) {
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO addTask(TaskDTO taskDTO) {
        Task task = convertToEntity(taskDTO);
        task.setDone(false);
        task.setId(null);
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();

        tasks.sort(
                Comparator
                        .comparingInt((Task t) -> {
                            switch (t.getPriority()) {
                                case "High": return 1;
                                case "Medium": return 2;
                                case "Low": return 3;
                                default: return 4;
                            }
                        })

                        .thenComparing(Task::getEndTime)
        );

        return convertToDTOList(tasks);
    }


    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id).map(this::convertToDTO);
    }

    public Optional<TaskDTO> updateTask(Long id, TaskDTO taskDetails) {
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
            return convertToDTO(taskRepository.save(existingTask));
        });
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<TaskDTO> toggleTaskStatus(Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setDone(!task.getDone());
            return convertToDTO(taskRepository.save(task));
        });
    }

    public List<TaskDTO> getTasksByStatus(boolean done) {
        return convertToDTOList(taskRepository.findByDone(done));
    }

    public List<TaskDTO> getTasksByPriority(String priority) {
        return convertToDTOList(taskRepository.findByPriority(priority));
    }

    public List<TaskDTO> getTasksByCategory(String category) {
        return convertToDTOList(taskRepository.findByCategory(category));
    }

    public List<TaskDTO> getOverdueTasks() {
        return convertToDTOList(taskRepository.findOverdueTasks(LocalDateTime.now()));
    }

    public List<TaskDTO> getTodayTasks() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return convertToDTOList(taskRepository.findTasksBetween(startOfDay, endOfDay));
    }

    public List<TaskDTO> getTasksStartingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        return convertToDTOList(taskRepository.findTasksStartingSoon(now, oneHourLater));
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