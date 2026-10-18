package com.taskservice.taskservice.dto;
import java.time.*;
import java.util.List;
import com.taskservice.taskservice.enums.*;
import com.taskservice.taskservice.entity.ChecklistItem;
public record TaskResponse(Long id,String title,String description,TaskStatus status,TaskPriority priority,LocalDate dueDate,LocalDateTime createdAt,LocalDateTime updatedAt,Long assignedUserId,Long projectId,long version,List<String> labels,List<ChecklistItem> checklist) {}
