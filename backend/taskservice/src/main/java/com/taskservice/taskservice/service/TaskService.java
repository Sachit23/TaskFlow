package com.taskservice.taskservice.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import com.taskservice.taskservice.dto.*;
import com.taskservice.taskservice.entity.*;
import com.taskservice.taskservice.repository.*;
import java.util.*;
@Service @Transactional
public class TaskService {
 private final TaskRepository tasks;private final ProjectRepository projects;private final CommentRepository comments;private final ActivityRepository activities;
 public TaskService(TaskRepository tasks,ProjectRepository projects,CommentRepository comments,ActivityRepository activities){this.tasks=tasks;this.projects=projects;this.comments=comments;this.activities=activities;}
 public List<TaskResponse> list(Long userId){return tasks.findByAssignedUserIdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();}
 public TaskResponse get(Long id,Long userId){return response(owned(id,userId));}
 public TaskResponse create(CreateTaskRequest r,Long userId){
  if(tasks.countByAssignedUserId(userId)>=5000)throw new ResponseStatusException(CONFLICT,"Workspace limit reached (5,000 tasks). Delete completed tasks to make room.");
  Task t=new Task();t.setAssignedUserId(userId);apply(t,r,userId);tasks.saveAndFlush(t);log(userId,"created task",t.getTitle(),t.getId());return response(t);
 }
 public TaskResponse update(Long id,UpdateTaskRequest r,Long userId){Task t=owned(id,userId);if(r.getVersion()!=t.getVersion())throw new ResponseStatusException(CONFLICT,"This task changed in another tab. Refresh before saving.");String action=t.getStatus()!=r.getStatus()?"moved task to "+r.getStatus().name().toLowerCase().replace('_',' '):"updated task";apply(t,r,userId);tasks.saveAndFlush(t);log(userId,action,t.getTitle(),t.getId());return response(t);}
 public void delete(Long id,Long userId){Task t=owned(id,userId);comments.deleteByTaskId(id);tasks.delete(t);log(userId,"deleted task",t.getTitle(),null);}
 public List<Project> projects(Long userId){return projects.findByOwnerIdOrderByCreatedAtAsc(userId);}
 public Project project(Long id,Long userId){return projects.findByIdAndOwnerId(id,userId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Project not found."));}
 public Project createProject(ProjectRequest r,Long userId){if(projects(userId).size()>=100)throw new ResponseStatusException(CONFLICT,"Workspace limit reached (100 projects).");Project p=new Project();p.setOwnerId(userId);setProject(p,r);projects.saveAndFlush(p);log(userId,"created project",p.getName(),null);return p;}
 public Project updateProject(Long id,ProjectRequest r,Long userId){Project p=project(id,userId);setProject(p,r);projects.save(p);log(userId,r.archived()?"archived project":"updated project",p.getName(),null);return p;}
 public void deleteProject(Long id,Long userId){Project p=project(id,userId);for(Task t:tasks.findByProjectIdAndAssignedUserId(id,userId))t.setProjectId(null);projects.delete(p);log(userId,"deleted project",p.getName(),null);}
 public List<TaskComment> comments(Long id,Long userId){owned(id,userId);return comments.findByTaskIdOrderByCreatedAtAsc(id);}
 public TaskComment comment(Long id,String body,UserClient.Identity user){owned(id,user.id());if(comments.findByTaskIdOrderByCreatedAtAsc(id).size()>=200)throw new ResponseStatusException(CONFLICT,"This task has reached its 200 comment limit.");TaskComment c=new TaskComment();c.setTaskId(id);c.setAuthor(user.name());c.setBody(body.trim());comments.saveAndFlush(c);log(user.id(),"commented on",owned(id,user.id()).getTitle(),id);return c;}
 public List<Activity> activity(Long userId){return activities.findTop50ByOwnerIdOrderByCreatedAtDesc(userId);}
 private void setProject(Project p,ProjectRequest r){p.setName(r.name().trim());p.setDescription(r.description());p.setColor(r.color());p.setArchived(r.archived());}
 private Task owned(Long id,Long userId){return tasks.findByIdAndAssignedUserId(id,userId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Task not found."));}
 private void apply(Task t,CreateTaskRequest r,Long userId){if(r.getProjectId()!=null){Project p=project(r.getProjectId(),userId);if(p.isArchived()&&!Objects.equals(t.getProjectId(),p.getId()))throw new ResponseStatusException(CONFLICT,"Restore this project before adding tasks.");}t.setTitle(r.getTitle().trim());t.setDescription(r.getDescription());t.setStatus(r.getStatus());t.setPriority(r.getPriority());t.setDueDate(r.getDueDate());t.setProjectId(r.getProjectId());t.getLabels().clear();if(r.getLabels()!=null)t.getLabels().addAll(r.getLabels().stream().map(String::trim).distinct().toList());t.getChecklist().clear();if(r.getChecklist()!=null){if(r.getChecklist().stream().map(ChecklistItem::getId).distinct().count()!=r.getChecklist().size())throw new ResponseStatusException(BAD_REQUEST,"Checklist identifiers must be unique.");t.getChecklist().addAll(r.getChecklist());}}
 private TaskResponse response(Task t){return new TaskResponse(t.getId(),t.getTitle(),t.getDescription(),t.getStatus(),t.getPriority(),t.getDueDate(),t.getCreatedAt(),t.getUpdatedAt(),t.getAssignedUserId(),t.getProjectId(),t.getVersion(),new ArrayList<>(t.getLabels()),new ArrayList<>(t.getChecklist()));}
 private void log(Long userId,String action,String subject,Long taskId){Activity a=new Activity();a.setOwnerId(userId);a.setAction(action);a.setSubject(subject);a.setTaskId(taskId);activities.save(a);}
}
