package com.taskservice.taskservice.entity;
import java.time.*;
import java.util.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.taskservice.taskservice.enums.*;
@Entity @Table(name="tasks") @Getter @Setter
public class Task {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=180) private String title;
 @Column(length=10000) private String description;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private TaskStatus status;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private TaskPriority priority;
 private LocalDate dueDate;
 @Column(nullable=false) private LocalDateTime createdAt;
 @Column(nullable=false) private LocalDateTime updatedAt;
 @Column(nullable=false) private Long assignedUserId;
 private Long projectId;
 @Version private long version;
 @ElementCollection @CollectionTable(name="task_labels",joinColumns=@JoinColumn(name="task_id")) @Column(name="label",length=30) private List<String> labels=new ArrayList<>();
 @ElementCollection @CollectionTable(name="task_checklist",joinColumns=@JoinColumn(name="task_id")) @OrderColumn(name="position") private List<ChecklistItem> checklist=new ArrayList<>();
 @PrePersist void create(){createdAt=LocalDateTime.now(java.time.Clock.systemUTC());updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now(java.time.Clock.systemUTC());}
}
