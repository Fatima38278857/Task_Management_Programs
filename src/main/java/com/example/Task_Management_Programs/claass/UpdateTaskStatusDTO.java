package com.example.Task_Management_Programs.claass;


import com.example.Task_Management_Programs.enumm.TaskStatus;
import lombok.Data;

@Data
public class UpdateTaskStatusDTO {
    private TaskStatus status;
}
