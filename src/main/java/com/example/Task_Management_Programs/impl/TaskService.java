package com.example.Task_Management_Programs.impl;



import com.example.Task_Management_Programs.claass.CommentRequest;
import com.example.Task_Management_Programs.claass.CreateOrUpdateTaskDTO;
import com.example.Task_Management_Programs.claass.UpdateTaskPriorityDTO;
import com.example.Task_Management_Programs.claass.UpdateTaskStatusDTO;
import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskService {

     void takeTask(Long taskId,   Long userId);

    CommentDTO addCommentToTask(Long taskId, CommentRequest commentRequest);

    Page<TaskDTO> getTasksByUserId(Long userId, Pageable pageable);
    Page<TaskDTO> getTasksWithFilters(String status, String priority, Pageable pageable);
    Page<TaskDTO> getTasksByExecutor(Long executorId, Pageable pageable);
    TaskDTO addTask(CreateOrUpdateTaskDTO properties, Long id);

    Optional<TaskEntity> getTask(Long id);
    void removeTask(Long id);

    TaskDTO updateTask(Long id, CreateOrUpdateTaskDTO properties, UserEntity currentUser);
    void updateTaskPriority(Long taskId, UpdateTaskPriorityDTO updateDto, UserEntity user);
    void updateTaskStatus(Long taskId, UpdateTaskStatusDTO updateDto, UserEntity user);

}
