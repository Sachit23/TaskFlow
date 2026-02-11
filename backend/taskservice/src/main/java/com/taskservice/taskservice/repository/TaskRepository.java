package com.taskservice.taskservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taskservice.taskservice.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedUserId(Long userId);

}
