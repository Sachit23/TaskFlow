package com.taskservice.taskservice.dto;

import java.time.LocalDate;

import com.taskservice.taskservice.enums.TaskPriority;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TaskPriority priority;

    @Future
    private LocalDate dueDate;
}
