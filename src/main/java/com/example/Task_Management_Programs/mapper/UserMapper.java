package com.example.Task_Management_Programs.mapper;


import com.example.Task_Management_Programs.dto.UserDTO;
import com.example.Task_Management_Programs.entity.UserEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(UserEntity user);
    UserEntity toEntity(UserDTO userDTO);
}
