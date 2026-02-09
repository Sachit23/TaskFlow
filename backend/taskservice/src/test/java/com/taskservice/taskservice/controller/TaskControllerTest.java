package com.taskservice.taskservice.controller;

import com.taskservice.taskservice.dto.CreateTaskRequest;
import com.taskservice.taskservice.dto.TaskResponse;
import com.taskservice.taskservice.dto.UpdateTaskRequest;
import com.taskservice.taskservice.enums.TaskPriority;
import com.taskservice.taskservice.enums.TaskStatus;
import com.taskservice.taskservice.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .build();
    }

    @Test
    void testCreateTask() throws Exception {
        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Create");
        response.setDescription("Test");
        response.setStatus(TaskStatus.PENDING);
        response.setPriority(TaskPriority.HIGH);
        response.setDueDate(LocalDate.now().plusDays(1));
        response.setCreatedAt(LocalDateTime.now());

        when(taskService.createTask(any(CreateTaskRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Create",
                                  "description": "Test",
                                  "priority": "HIGH",
                                  "dueDate": "2030-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

}
