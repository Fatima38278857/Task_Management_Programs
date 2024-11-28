package com.example.Task_Management_Programs.repository;


import com.example.Task_Management_Programs.entity.CommentEntity;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Optional<CommentEntity> findCommentById(Long id);
    List<TaskEntity> findByUserId(UserEntity userId);
    List<TaskEntity> findByUserId(Long userId);

}
