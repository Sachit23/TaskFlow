package com.taskservice.taskservice.repository;
import java.util.*;
import com.taskservice.taskservice.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRepository extends JpaRepository<Project,Long> { List<Project> findByOwnerIdOrderByCreatedAtAsc(Long ownerId); Optional<Project> findByIdAndOwnerId(Long id,Long ownerId); }
