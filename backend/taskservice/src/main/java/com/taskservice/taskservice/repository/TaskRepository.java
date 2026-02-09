package com.taskservice.taskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskservice.taskservice.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
