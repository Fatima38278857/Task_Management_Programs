package com.example.Task_Management_Programs.dto;


import com.example.Task_Management_Programs.enumm.Role;
import com.example.Task_Management_Programs.enumm.TaskPriority;
import com.example.Task_Management_Programs.enumm.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    @Size(min = 4, max = 32)
    @Schema(description = "заголовок объявления")
    private String title;
    @Size(min = 8, max = 64)
    @Schema(description = "описание объявления")
    private String description;
    @Schema(description = "Статус")
    private TaskStatus status;
    @Schema(description = "Приоритет")
    private TaskPriority priority;
    private Long userId;
    private List<CommentDTO> comment;
    @Schema(description = "Роль")
    private Role role;
}
