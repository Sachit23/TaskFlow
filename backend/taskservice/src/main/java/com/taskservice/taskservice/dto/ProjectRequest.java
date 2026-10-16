package com.taskservice.taskservice.dto;
import jakarta.validation.constraints.*;
public record ProjectRequest(@NotBlank @Size(max=80) String name,@Size(max=1000) String description,@NotNull @Pattern(regexp="^#[0-9a-fA-F]{6}$") String color,boolean archived) {}
