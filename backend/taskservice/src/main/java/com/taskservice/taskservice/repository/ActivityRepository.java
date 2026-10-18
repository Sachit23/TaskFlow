package com.taskservice.taskservice.repository;
import java.util.*;
import com.taskservice.taskservice.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ActivityRepository extends JpaRepository<Activity,Long> { List<Activity> findTop50ByOwnerIdOrderByCreatedAtDesc(Long ownerId); }
