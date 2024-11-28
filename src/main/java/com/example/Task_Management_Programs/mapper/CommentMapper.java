package com.example.Task_Management_Programs.mapper;


import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);


    CommentDTO toDTO(CommentEntity comment);

    CommentEntity toEntity(CommentDTO commentDTO);
}
