package com.example.Task_Management_Programs.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private String text;
    private Long userId;
    private Long taskId;
    private LocalDateTime сreatedAt;


}
