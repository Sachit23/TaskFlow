package com.taskservice.taskservice.repository;
import java.util.*;
import com.taskservice.taskservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TaskRepository extends JpaRepository<Task,Long> {
 List<Task> findByAssignedUserIdOrderByCreatedAtDesc(Long userId);
 Optional<Task> findByIdAndAssignedUserId(Long id,Long userId);
 List<Task> findByProjectIdAndAssignedUserId(Long projectId,Long userId);
 long countByAssignedUserId(Long userId);
}
