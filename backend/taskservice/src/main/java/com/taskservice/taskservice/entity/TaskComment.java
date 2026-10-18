package com.taskservice.taskservice.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name="task_comments") @Getter @Setter
public class TaskComment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long taskId;
 @Column(nullable=false,length=80) private String author;
 @Column(nullable=false,length=2000) private String body;
 @Column(nullable=false) private LocalDateTime createdAt;
 @PrePersist void create(){createdAt=LocalDateTime.now(java.time.Clock.systemUTC());}
}
