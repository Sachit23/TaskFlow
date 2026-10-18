package com.taskservice.taskservice.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
@Embeddable @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChecklistItem {
 @NotBlank @Size(max=64) @Column(name="item_id",length=64) private String id;
 @NotBlank @Size(max=200) @Column(name="item_title",length=200) private String title;
 @Column(name="completed",nullable=false) private boolean done;
}
