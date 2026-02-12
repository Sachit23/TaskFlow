package com.taskservice.taskservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskservice.taskservice.enums.TaskPriority;
import com.taskservice.taskservice.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private Long assignedUserId;
}
