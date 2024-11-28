package com.example.Task_Management_Programs.mapper;



import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TaskMapper {


    TaskDTO taskToTaskDTO(TaskEntity task);


    TaskEntity taskDTOToTask(TaskDTO taskDTO);
}
