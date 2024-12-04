package com.example.Task_Management_Programs.entity;


import com.example.Task_Management_Programs.enumm.TaskPriority;
import com.example.Task_Management_Programs.enumm.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne
    @JoinColumn(name = "users_id",  nullable = false)
    @JsonIgnore
    private UserEntity user;
    @OneToOne
    @JoinColumn(name = "executor_id")
    private UserEntity userExecutor;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentEntity> comments;

    public TaskEntity(String title, String description, TaskStatus status, TaskPriority priority, UserEntity user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.user = user;
        this.comments = new ArrayList<>();


    }
}
