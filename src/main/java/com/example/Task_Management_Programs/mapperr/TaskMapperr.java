package com.example.Task_Management_Programs.mapperr;


import com.example.Task_Management_Programs.claass.CreateOrUpdateTaskDTO;
import com.example.Task_Management_Programs.claass.TaskWithCommentsDTO;
import com.example.Task_Management_Programs.dto.CommentDTO;

import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.CommentEntity;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/*

 */
@Component
public class TaskMapperr {
    private final CommentMapperr commentMapper;
    private final UserRepository userRepository;

    public TaskMapperr(CommentMapperr commentMapper, UserRepository userRepository) {
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
    }

    public TaskEntity createOrUpdateAdToAd(CreateOrUpdateTaskDTO dto, UserEntity author) {
        return new TaskEntity(dto.getTitle(), dto.getDescription(), dto.getStatus(), dto.getTaskPriority(), author);
    }

    public TaskDTO taskDTO(TaskEntity taskEntity) {
        if (taskEntity == null) {
            return null;
        }
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskEntity.getId());
        taskDTO.setTitle(taskEntity.getTitle());
        taskDTO.setDescription(taskEntity.getDescription());
        taskDTO.setStatus(taskEntity.getStatus());
        taskDTO.setPriority(taskEntity.getPriority());
        taskDTO.setUserId(taskEntity.getUser().getId());
        if (taskEntity.getComments() != null) {
            CommentMapperr commentMapper = new CommentMapperr();
            List<CommentDTO> comments = taskEntity.getComments().stream()
                    .map(commentMapper::toDTO)
                    .collect(Collectors.toList());
            taskDTO.setComment(comments);
        }
        return taskDTO;
    }
    public TaskEntity taskEntity(TaskDTO taskDTO) {
        if (taskDTO == null) {
            return null;
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(taskDTO.getId());
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus());
        taskEntity.setPriority(taskDTO.getPriority());
        Long userId = taskDTO.getUserId();
        if (userId != null) {
            Optional<UserEntity> userEntityOpt = userRepository.findById(userId);
            if (userEntityOpt.isPresent()) {
                taskEntity.setUser(userEntityOpt.get());
            } else {
                throw new EntityNotFoundException("User not found with id: " + userId);
            }
        } else {
            taskEntity.setUser(null);
        }
        if (taskDTO.getComment() != null) {
            List<CommentEntity> comments = taskDTO.getComment().stream()
                    .map(commentDTO -> {
                        CommentEntity commentEntity = commentMapper.toEntity(commentDTO);
                        commentEntity.setTask(taskEntity);  // Установим связь с задачей
                        return commentEntity;
                    })
                    .collect(Collectors.toList());
            taskEntity.setComments(comments);
        }
        return taskEntity;

}
    public TaskWithCommentsDTO taskToTaskWithCommentsDTO(TaskEntity task, List<CommentEntity> comments) {
        TaskWithCommentsDTO dto = new TaskWithCommentsDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setUserId(task.getUser());

        List<CommentDTO> commentDTOs = comments.stream()
                .map(this::commentToCommentDTO)
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);
        return dto;
    }

    private CommentDTO commentToCommentDTO(CommentEntity comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setTaskId(comment.getTask().getId());
        return dto;
    }
}