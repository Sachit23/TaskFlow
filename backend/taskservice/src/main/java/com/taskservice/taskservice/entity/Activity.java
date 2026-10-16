package com.taskservice.taskservice.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name="activity") @Getter @Setter
public class Activity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long ownerId;
 @Column(nullable=false,length=240) private String action;
 @Column(length=180) private String subject;
 private Long taskId;
 @Column(nullable=false) private LocalDateTime createdAt;
 @PrePersist void create(){createdAt=LocalDateTime.now(java.time.Clock.systemUTC());}
}
