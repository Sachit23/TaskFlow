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

    private tools.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .build();
        
        objectMapper = new tools.jackson.databind.ObjectMapper();
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

        mockMvc.perform(post("/api/tasks/create")
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

    @Test
    void testGetTasksByUser() throws Exception {

        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Task1");
        response.setStatus(TaskStatus.PENDING);
        response.setPriority(TaskPriority.HIGH);
        response.setDueDate(LocalDate.now());
        response.setCreatedAt(LocalDateTime.now());

        when(taskService.getTasksByUser(5L))
                .thenReturn(java.util.List.of(response));

        mockMvc.perform(get("/api/tasks/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task1"));
    }

    @Test
    void testUpdateTask() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Desc");
        request.setPriority(TaskPriority.MEDIUM);
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDate.of(2030,1,1));

        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Updated Task");
        response.setStatus(TaskStatus.IN_PROGRESS);
        response.setPriority(TaskPriority.MEDIUM);

        when(taskService.updateTask(eq(1L), any(UpdateTaskRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

}
