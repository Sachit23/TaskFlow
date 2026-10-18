package com.taskservice.taskservice.repository;
import java.util.*;
import com.taskservice.taskservice.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<TaskComment,Long> { List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId); void deleteByTaskId(Long taskId); }
