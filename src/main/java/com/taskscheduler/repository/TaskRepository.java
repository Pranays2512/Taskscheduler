package com.taskscheduler.repository;

import com.taskscheduler.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks by completion status
    List<Task> findByDone(Boolean done);

    // Find tasks by priority
    List<Task> findByPriority(String priority);

    // Find tasks by category
    List<Task> findByCategory(String category);

    // Find tasks within a date range
    @Query("SELECT t FROM Task t WHERE t.startTime >= :startDate AND t.endTime <= :endDate")
    List<Task> findTasksInDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Find overdue tasks (not done and end time has passed)
    @Query("SELECT t FROM Task t WHERE t.done = false AND t.endTime < :currentTime")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    // Find tasks starting soon
    @Query("SELECT t FROM Task t WHERE t.done = false AND t.startTime BETWEEN :now AND :future")
    List<Task> findTasksStartingSoon(@Param("now") LocalDateTime now,
                                     @Param("future") LocalDateTime future);

    // Find today's tasks
    @Query("SELECT t FROM Task t WHERE DATE(t.startTime) = DATE(:today)")
    List<Task> findTodayTasks(@Param("today") LocalDateTime today);

    // Count tasks by status
    Long countByDone(Boolean done);

    // Count tasks by priority
    Long countByPriority(String priority);

    // Count tasks by category
    Long countByCategory(String category);
}
