package com.taskservice.taskservice.controller;
import com.taskservice.taskservice.service.*;
import com.taskservice.taskservice.dto.*;
import com.taskservice.taskservice.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/api")
public class TaskController {
 private final TaskService tasks;public TaskController(TaskService tasks){this.tasks=tasks;}
 @GetMapping("/tasks") public List<TaskResponse> list(@RequestAttribute("identity")UserClient.Identity u){return tasks.list(u.id());}
 @GetMapping("/tasks/{id}") public TaskResponse get(@PathVariable Long id,@RequestAttribute("identity")UserClient.Identity u){return tasks.get(id,u.id());}
 @PostMapping({"/tasks","/tasks/create"}) @ResponseStatus(HttpStatus.CREATED) public TaskResponse create(@Valid @RequestBody CreateTaskRequest r,@RequestAttribute("identity")UserClient.Identity u){return tasks.create(r,u.id());}
 @PutMapping("/tasks/{id}") public TaskResponse update(@PathVariable Long id,@Valid @RequestBody UpdateTaskRequest r,@RequestAttribute("identity")UserClient.Identity u){return tasks.update(id,r,u.id());}
 @DeleteMapping("/tasks/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id,@RequestAttribute("identity")UserClient.Identity u){tasks.delete(id,u.id());}
 @GetMapping("/projects") public List<Project> projects(@RequestAttribute("identity")UserClient.Identity u){return tasks.projects(u.id());}
 @PostMapping("/projects") @ResponseStatus(HttpStatus.CREATED) public Project createProject(@Valid @RequestBody ProjectRequest r,@RequestAttribute("identity")UserClient.Identity u){return tasks.createProject(r,u.id());}
 @PutMapping("/projects/{id}") public Project updateProject(@PathVariable Long id,@Valid @RequestBody ProjectRequest r,@RequestAttribute("identity")UserClient.Identity u){return tasks.updateProject(id,r,u.id());}
 @DeleteMapping("/projects/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteProject(@PathVariable Long id,@RequestAttribute("identity")UserClient.Identity u){tasks.deleteProject(id,u.id());}
 @GetMapping("/activity") public List<Activity> activity(@RequestAttribute("identity")UserClient.Identity u){return tasks.activity(u.id());}
 public record CommentRequest(@NotBlank @Size(max=2000) String body) {}
 @GetMapping("/tasks/{id}/comments") public List<TaskComment> comments(@PathVariable Long id,@RequestAttribute("identity")UserClient.Identity u){return tasks.comments(id,u.id());}
 @PostMapping("/tasks/{id}/comments") @ResponseStatus(HttpStatus.CREATED) public TaskComment comment(@PathVariable Long id,@Valid @RequestBody CommentRequest r,@RequestAttribute("identity")UserClient.Identity u){return tasks.comment(id,r.body(),u);}
}
