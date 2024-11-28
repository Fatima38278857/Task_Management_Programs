package com.example.Task_Management_Programs.dto;


import com.example.Task_Management_Programs.enumm.Role;
import lombok.Data;



@Data
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private Role role;
}
