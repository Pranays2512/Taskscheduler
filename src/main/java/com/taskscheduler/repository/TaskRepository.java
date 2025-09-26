//src/main/java/com/taskscheduler/repository/TaskRepository.java
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

    List<Task> findByDone(Boolean done);

    List<Task> findByPriority(String priority);

    List<Task> findByCategory(String category);


    @Query("SELECT t FROM Task t WHERE t.done = false AND t.endTime < :currentTime")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Task t WHERE t.done = false AND t.startTime BETWEEN :now AND :future")
    List<Task> findTasksStartingSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    @Query("SELECT t FROM Task t WHERE t.startTime >= :start AND t.startTime <= :end")
    List<Task> findTasksBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    Long countByDone(Boolean done);

    Long countByPriority(String priority);

    Long countByCategory(String category);

    @Query("SELECT count(t) FROM Task t WHERE t.done = false AND t.endTime < :currentTime")
    long countOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT count(t) FROM Task t WHERE t.startTime >= :start AND t.startTime <= :end")
    long countTasksBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}