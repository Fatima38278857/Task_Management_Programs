package com.example.Task_Management_Programs.claass;


import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.entity.UserEntity;
import lombok.Data;

import java.util.List;
@Data
public class TaskWithCommentsDTO {

    private Long id;
    private String title;
    private String description;
    private UserEntity userId;
    private List<CommentDTO> comments;
}
