package com.example.Task_Management_Programs.controller;


import com.example.Task_Management_Programs.claass.*;
import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.mapperImpl.TaskMapperImpl;
import com.example.Task_Management_Programs.repository.TaskRepository;
import com.example.Task_Management_Programs.service.TaskImpl;
import com.example.Task_Management_Programs.service.UserImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskImpl taskService;

    @Autowired
    private UserImpl userService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapperImpl taskMapperImpl;

    @PreAuthorize("hasRole('AUTHOR')")
    @PostMapping("/assign-task")
    @Operation(summary = "Назначить задачу пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно назначена"),
                    @ApiResponse(responseCode = "404", description = "Задача или пользователь не найдены"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные запроса")
            })
    public ResponseEntity<String> assignTask(@RequestBody AssignTaskRequest request) {
        try {
            taskService.takeTask(request.getTaskId(), request.getUserId());
            return ResponseEntity.ok("Исполнитель задачи успешно назначен");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "/user/{userId}", params = {})
    @Operation(summary = "Получить задачи пользователя и их комментарии",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно получены"),
                    @ApiResponse(responseCode = "404", description = "User не найден")
            })
    public Page<TaskDTO> getTasksByUserId(@PathVariable Long userId, Pageable pageable) {
        return taskService.getTasksByUserId(userId, pageable);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Получить задачи с фильтрацией по статусу и/или приоритету")
    public Page<TaskDTO> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Pageable pageable) {

        return taskService.getTasksWithFilters(status, priority, pageable);
    }

    @GetMapping("/executor/{executorId}")
    @Operation(summary = "Получить задачи по Id исполнителя с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно получены"),
                    @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
            })
    public Page<TaskDTO> getTasksByExecutor(@PathVariable Long executorId, @PageableDefault(size = 10) Pageable pageable) {
        return taskService.getTasksByExecutor(executorId, pageable);
    }


    @PostMapping("/{taskId}/comments")
    @Operation(summary = "Дабавить комментарии к задаче",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий успешно добавлен"),
                    @ApiResponse(responseCode = "404", description = "Такой задачи нет")
            })
    public ResponseEntity<CommentDTO> addCommentToTask(
            @PathVariable Long taskId,
            @RequestBody CommentRequest commentRequest) {
        System.out.println("Request UserEmail: " + commentRequest.getUserEmail());
        System.out.println("Request CommentText: " + commentRequest.getText());
        CommentDTO commentDTO = taskService.addCommentToTask(taskId, commentRequest);
        return new ResponseEntity<>(commentDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('AUTHOR')")
    @PostMapping("/add")
    @Operation(summary = "Create a new task",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные запроса")
            })
    public ResponseEntity<TaskDTO> addTask(@RequestBody CreateOrUpdateTaskDTO properties, @RequestParam Long userId, Authentication authentication) {
        log.info("addAd in TaskController is used");
        TaskDTO createdTask = taskService.addTask(properties, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получит задачу по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно получена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            })
    public ResponseEntity<TaskEntity> getTask(@PathVariable Long id) {
        Optional<TaskEntity> task = taskService.getTask(id);
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}/priority")
    @Operation(summary = "Обновить приоритет задачи",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Приоритет успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            })
    public void updateTaskPriority(
            @PathVariable Long id,
            @RequestBody UpdateTaskPriorityDTO updateDto,
            @RequestHeader("User-Id") Long userId) {

        UserEntity user = userService.findById(userId);
        taskService.updateTaskPriority(id, updateDto, user);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Обновить статус задачи",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Статус успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            })
    public void updateTaskStatus(
            @PathVariable Long id,
            @RequestBody UpdateTaskStatusDTO updateDto,
            @RequestHeader("User-Id") Long userId) {

        UserEntity user = userService.findById(userId);
        taskService.updateTaskStatus(id, updateDto, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задачу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно обновленна"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена "),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            })
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody CreateOrUpdateTaskDTO createOrUpdateTaskDTO,
            @AuthenticationPrincipal UserEntity currentUser) {
        TaskDTO updatedTask = taskService.updateTask(id, createOrUpdateTaskDTO, currentUser);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "Удаление задачи",
            description = "Удаляет задачу по указанному идентификатору. Требует аутентификации.",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "id", description = "Уникальный идентификатор задачи", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeTask(@PathVariable Long id) {
        taskService.removeTask(id);
        return ResponseEntity.noContent().build();
    }
}
