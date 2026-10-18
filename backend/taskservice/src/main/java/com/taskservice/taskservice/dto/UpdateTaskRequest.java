package com.taskservice.taskservice.dto;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
@Getter @Setter public class UpdateTaskRequest extends CreateTaskRequest { @NotNull private Long version; }
