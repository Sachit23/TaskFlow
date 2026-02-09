package com.taskservice.taskservice.dto;

import com.taskservice.taskservice.enums.TaskPriority;
import com.taskservice.taskservice.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TaskStatus status;

    @NotNull
    private TaskPriority priority;

    @FutureOrPresent
    private LocalDate dueDate;
}
