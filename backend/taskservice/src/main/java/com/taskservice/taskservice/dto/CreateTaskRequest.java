package com.taskservice.taskservice.dto;
import java.time.LocalDate;
import java.util.*;
import com.taskservice.taskservice.enums.*;
import com.taskservice.taskservice.entity.ChecklistItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class CreateTaskRequest {
 @NotBlank @Size(max=180) private String title;
 @Size(max=10000) private String description;
 @NotNull private TaskPriority priority=TaskPriority.MEDIUM;
 @NotNull private TaskStatus status=TaskStatus.PENDING;
 private LocalDate dueDate;
 private Long projectId;
 @Size(max=8) private List<@NotBlank @Size(max=30) String> labels=new ArrayList<>();
 @Valid @Size(max=30) private List<ChecklistItem> checklist=new ArrayList<>();
}
