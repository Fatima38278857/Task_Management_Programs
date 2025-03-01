package com.example.Task_Management_Programs.service;

import com.example.Task_Management_Programs.claass.CommentRequest;
import com.example.Task_Management_Programs.claass.CreateOrUpdateTaskDTO;
import com.example.Task_Management_Programs.claass.UpdateTaskPriorityDTO;
import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.CommentEntity;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.enums.Role;
import com.example.Task_Management_Programs.enums.TaskPriority;
import com.example.Task_Management_Programs.enums.TaskStatus;

import com.example.Task_Management_Programs.exception.NoRightsException;
import com.example.Task_Management_Programs.exception.TaskNotFoundException;
import com.example.Task_Management_Programs.exception.UserNotFoundException;
import com.example.Task_Management_Programs.impl.TaskService;
import com.example.Task_Management_Programs.impl.UserService;
import com.example.Task_Management_Programs.mapperImpl.CommentMapperImpl;
import com.example.Task_Management_Programs.mapperImpl.TaskMapperImpl;
import com.example.Task_Management_Programs.repository.CommentRepository;
import com.example.Task_Management_Programs.repository.TaskRepository;
import com.example.Task_Management_Programs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TaskImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskMapperImpl taskMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapperImpl commentMapper;

    private TaskService taskService; // Без @InjectMocks!

    @BeforeEach
    void setUp() {
        taskService = new TaskImpl(taskRepository, userService, userRepository, taskMapper, commentRepository, commentMapper); // Создаём вручную
    }

    @Test
    void shouldReturnTasksByUserId() {
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");

        List<TaskEntity> mockTasks = List.of(
                new TaskEntity("Task 1", "Description 1", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, mockUser),
                new TaskEntity("Task 2", "Description 2", TaskStatus.COMPLETED, TaskPriority.LOW, mockUser)
        );

        Mockito.when(taskRepository.findByUserId(userId, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(mockTasks));


        Mockito.when(taskMapper.taskDTO(any(TaskEntity.class)))
                .thenAnswer(invocation -> {
                    TaskEntity task = invocation.getArgument(0);
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setUserId(task.getUser().getId());
                    return taskDTO;
                });


        Page<TaskDTO> result = taskService.getTasksByUserId(userId, Pageable.unpaged());

        // ПРОВЕРКИ
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals("Task 1", result.getContent().get(0).getTitle());

        Mockito.verify(taskRepository, Mockito.times(1)).findByUserId(userId, Pageable.unpaged());
    }


    @Test
    void testGetTasksWithFilters() {
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");
        // Подготовка данных
        TaskStatus statusInProgress = TaskStatus.IN_PROGRESS;
        TaskPriority priorityHigh = TaskPriority.HIGH;

        TaskEntity task1 = new TaskEntity("Task 1", "Description 1", statusInProgress, priorityHigh, mockUser);
        TaskEntity task2 = new TaskEntity("Task 2", "Description 2", TaskStatus.COMPLETED, TaskPriority.LOW, mockUser);

        List<TaskEntity> taskList = List.of(task1, task2);

        // Мокирование репозитория: возвращаем задачи с заданными фильтрами
        Mockito.when(taskRepository.findByFilters(statusInProgress, priorityHigh, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(task1)));

        // Мокирование маппера: преобразуем задачи в DTO
        Mockito.when(taskMapper.taskDTO(any(TaskEntity.class)))
                .thenAnswer(invocation -> {
                    TaskEntity task = invocation.getArgument(0);
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setUserId(task.getUser().getId());
                    return taskDTO;
                });


        Page<TaskDTO> result = taskService.getTasksWithFilters("IN_PROGRESS", "HIGH", Pageable.unpaged());

        // Проверки
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty()); // Убедимся, что результат не пуст
        assertEquals(1, result.getTotalElements()); // Всего одна задача, подходящая под фильтры
        assertEquals("Task 1", result.getContent().get(0).getTitle()); // Проверим, что это именно "Task 1"
        assertEquals(TaskStatus.IN_PROGRESS, result.getContent().get(0).getStatus());
        assertEquals(TaskPriority.HIGH, result.getContent().get(0).getPriority());

        // Проверка вызова репозитория
        Mockito.verify(taskRepository, Mockito.times(1)).findByFilters(statusInProgress, priorityHigh, Pageable.unpaged());
    }

    @Test
    void testGetTasksWithFiltersNoResults() {

        TaskStatus statusCompleted = TaskStatus.COMPLETED;
        TaskPriority priorityLow = TaskPriority.LOW;

        Mockito.when(taskRepository.findByFilters(statusCompleted, priorityLow, Pageable.unpaged()))
                .thenReturn(Page.empty());


        Page<TaskDTO> result = taskService.getTasksWithFilters("COMPLETED", "LOW", Pageable.unpaged());


        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        // Проверка вызова репозитория
        Mockito.verify(taskRepository, Mockito.times(1)).findByFilters(statusCompleted, priorityLow, Pageable.unpaged());
    }

    @Test
    void testGetTasksWithFiltersNoFilters() {
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");
        // Подготовка данных
        TaskEntity task1 = new TaskEntity("Task 1", "Description 1", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, mockUser);
        TaskEntity task2 = new TaskEntity("Task 2", "Description 2", TaskStatus.COMPLETED, TaskPriority.LOW, mockUser);

        List<TaskEntity> taskList = List.of(task1, task2);

        // Мокирование репозитория: возвращаем все задачи
        Mockito.when(taskRepository.findByFilters(null, null, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(taskList)); // Возвращаем страницу с двумя задачами

        // Мокирование маппера: преобразуем задачи в DTO
        Mockito.when(taskMapper.taskDTO(any(TaskEntity.class)))
                .thenAnswer(invocation -> {
                    TaskEntity task = invocation.getArgument(0);
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setUserId(task.getUser().getId());
                    return taskDTO;
                });

        // Вызов тестируемого метода без фильтров
        Page<TaskDTO> result = taskService.getTasksWithFilters(null, null, Pageable.unpaged());

        // Проверки
        assertNotNull(result); // Убедимся, что результат не null
        assertFalse(result.getContent().isEmpty()); // Убедимся, что результат не пустой
        assertEquals(2, result.getTotalElements()); // Должно быть 2 задачи
        assertEquals("Task 1", result.getContent().get(0).getTitle()); // Проверим первую задачу
        assertEquals("Task 2", result.getContent().get(1).getTitle()); // Проверим вторую задачу

        // Проверка вызова репозитория
        Mockito.verify(taskRepository, Mockito.times(1)).findByFilters(null, null, Pageable.unpaged());
    }

    @Test
    void testGetTasksByExecutor() {
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");
        // Подготовка данных
        Long executorId = 1L;
        TaskEntity task1 = new TaskEntity("Task 1", "Description 1", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, mockUser);
        TaskEntity task2 = new TaskEntity("Task 2", "Description 2", TaskStatus.COMPLETED, TaskPriority.LOW, mockUser);

        // Список задач
        List<TaskEntity> taskList = List.of(task1, task2);

        // Мокирование репозитория: возвращаем страницу с задачами для данного executorId
        Mockito.when(taskRepository.findByUserExecutorId(executorId, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(taskList)); // Возвращаем страницу с двумя задачами

        // Мокирование маппера: преобразуем задачи в DTO
        Mockito.when(taskMapper.taskDTO(any(TaskEntity.class)))
                .thenAnswer(invocation -> {
                    TaskEntity task = invocation.getArgument(0);
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setUserId(task.getUser().getId());
                    return taskDTO;
                });

        // Вызов тестируемого метода
        Page<TaskDTO> result = taskService.getTasksByExecutor(executorId, Pageable.unpaged());

        // Проверки
        assertNotNull(result); // Убедимся, что результат не null
        assertFalse(result.getContent().isEmpty()); // Убедимся, что результат не пустой
        assertEquals(2, result.getTotalElements()); // Должно быть 2 задачи
        assertEquals("Task 1", result.getContent().get(0).getTitle()); // Проверим первую задачу
        assertEquals("Task 2", result.getContent().get(1).getTitle()); // Проверим вторую задачу

        // Проверка вызова репозитория
        Mockito.verify(taskRepository, Mockito.times(1)).findByUserExecutorId(executorId, Pageable.unpaged());
    }

    @Test
    void testAddTask_Success() {
        // ДАННЫЕ
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setRole(Role.AUTHOR); // Роль AUTHOR

        CreateOrUpdateTaskDTO taskProperties = new CreateOrUpdateTaskDTO();
        taskProperties.setTitle("New Task");
        taskProperties.setDescription("Task description");

        TaskEntity mockTaskEntity = new TaskEntity();
        mockTaskEntity.setTitle(taskProperties.getTitle());
        mockTaskEntity.setDescription(taskProperties.getDescription());
        mockTaskEntity.setUser(mockUser);

        TaskEntity savedTaskEntity = new TaskEntity();
        savedTaskEntity.setId(100L);
        savedTaskEntity.setTitle(taskProperties.getTitle());
        savedTaskEntity.setDescription(taskProperties.getDescription());
        savedTaskEntity.setUser(mockUser);

        TaskDTO expectedTaskDTO = new TaskDTO();
        expectedTaskDTO.setId(savedTaskEntity.getId());
        expectedTaskDTO.setTitle(savedTaskEntity.getTitle());
        expectedTaskDTO.setDescription(savedTaskEntity.getDescription());

        // МОКИРОВАНИЕ
        Mockito.when(userService.findById(userId)).thenReturn(mockUser); // Пользователь найден
        Mockito.when(taskMapper.createOrUpdateAdToAd(taskProperties, mockUser)).thenReturn(mockTaskEntity);
        Mockito.when(taskRepository.save(mockTaskEntity)).thenReturn(savedTaskEntity);
        Mockito.when(taskMapper.taskDTO(savedTaskEntity)).thenReturn(expectedTaskDTO);

        // ВЫЗОВ
        TaskDTO result = taskService.addTask(taskProperties, userId);

        // ПРОВЕРКИ
        assertNotNull(result);
        assertEquals(expectedTaskDTO.getId(), result.getId());
        assertEquals(expectedTaskDTO.getTitle(), result.getTitle());
        assertEquals(expectedTaskDTO.getDescription(), result.getDescription());

        // ПРОВЕРЯЕМ ВЫЗОВЫ
        Mockito.verify(userService, Mockito.times(1)).findById(userId);
        Mockito.verify(taskMapper, Mockito.times(1)).createOrUpdateAdToAd(taskProperties, mockUser);
        Mockito.verify(taskRepository, Mockito.times(1)).save(mockTaskEntity);
        Mockito.verify(taskMapper, Mockito.times(1)).taskDTO(savedTaskEntity);
    }

    @Test
    void testAddTask_AccessDenied() {
        // ДАННЫЕ
        Long userId = 1L;
        UserEntity mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setRole(Role.EXECUTOR); // НЕ AUTHOR

        CreateOrUpdateTaskDTO taskProperties = new CreateOrUpdateTaskDTO();


        Mockito.when(userService.findById(userId)).thenReturn(mockUser);


        assertThrows(AccessDeniedException.class, () -> taskService.addTask(taskProperties, userId));


        Mockito.verify(taskMapper, Mockito.never()).createOrUpdateAdToAd(any(), any());
        Mockito.verify(taskRepository, Mockito.never()).save(any());
        Mockito.verify(taskMapper, Mockito.never()).taskDTO(any());
    }

    @Test
    void testAddTask_UserNotFound() {

        Long userId = 1L;
        CreateOrUpdateTaskDTO taskProperties = new CreateOrUpdateTaskDTO();

        // МОКИРОВАНИЕ
        Mockito.when(userService.findById(userId)).thenReturn(null); // Пользователь НЕ найден

        // ВЫЗОВ + ОЖИДАНИЕ ИСКЛЮЧЕНИЯ
        assertThrows(NullPointerException.class, () -> taskService.addTask(taskProperties, userId));

        // ПРОВЕРЯЕМ, ЧТО ДАЛЬШЕ МЕТОД НЕ РАБОТАЛ
        Mockito.verify(taskMapper, Mockito.never()).createOrUpdateAdToAd(any(), any());
        Mockito.verify(taskRepository, Mockito.never()).save(any());
        Mockito.verify(taskMapper, Mockito.never()).taskDTO(any());
    }

    @Test
    void testTakeTask_Success() {
        // ДАННЫЕ
        Long taskId = 1L;
        Long executorId = 2L;

        UserEntity author = new UserEntity();
        author.setId(10L);
        author.setRole(Role.AUTHOR); // Авторизованный пользователь - AUTHOR

        UserEntity executor = new UserEntity();
        executor.setId(executorId);
        executor.setRole(Role.EXECUTOR); // Исполнитель с ролью EXECUTOR

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(author); // Автор задачи - тот же, кто выполняет запрос

        // МОКИРОВАНИЕ
        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author); // Авторизованный пользователь - AUTHOR
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task)); // Задача найдена
        Mockito.when(userRepository.findById(executorId)).thenReturn(Optional.of(executor)); // Исполнитель найден

        // ВЫЗОВ МЕТОДА
        taskService.takeTask(taskId, executorId);

        // ПРОВЕРКИ
        assertEquals(executor, task.getUserExecutor()); // Проверяем, что у задачи назначился исполнитель
        assertEquals(task, executor.getCurrentTask()); // Проверяем, что у исполнителя обновилось поле currentTask

        // ПРОВЕРЯЕМ, что save был вызван ровно 1 раз для обоих репозиториев
        Mockito.verify(taskRepository, Mockito.times(1)).save(task);
        Mockito.verify(userRepository, Mockito.times(1)).save(executor);
    }
    @Test
    void testTakeTask_AccessDenied_NotAuthor() {
        // ДАННЫЕ
        Long taskId = 1L;
        Long executorId = 2L;

        UserEntity nonAuthorUser = new UserEntity();
        nonAuthorUser.setId(10L);
        nonAuthorUser.setRole(Role.EXECUTOR); // НЕ AUTHOR

        // МОКИРОВАНИЕ
        Mockito.when(userService.getAuthenticatedUser()).thenReturn(nonAuthorUser); // Авторизованный пользователь - НЕ AUTHOR

        // ВЫЗОВ + ОЖИДАНИЕ ИСКЛЮЧЕНИЯ
        assertThrows(AccessDeniedException.class, () -> taskService.takeTask(taskId, executorId));

        // ПРОВЕРЯЕМ, что больше никаких вызовов НЕ БЫЛО
        Mockito.verify(taskRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(taskRepository, Mockito.never()).save(any());
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }
    @Test
    void testTakeTask_TaskNotFound() {
        Long taskId = 1L;
        Long executorId = 2L;

        UserEntity author = new UserEntity();
        author.setId(10L);
        author.setRole(Role.AUTHOR);

        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author);
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.empty()); // Задача НЕ найдена

        assertThrows(EntityNotFoundException.class, () -> taskService.takeTask(taskId, executorId));

        Mockito.verify(taskRepository, Mockito.never()).save(any());
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void testTakeTask_TaskNotOwnedByUser() {
        Long taskId = 1L;
        Long executorId = 2L;

        UserEntity author = new UserEntity();
        author.setId(10L);
        author.setRole(Role.AUTHOR);

        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(20L); // Другой владелец задачи

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(anotherUser); // Задача принадлежит другому пользователю

        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author);
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.takeTask(taskId, executorId));

        Mockito.verify(taskRepository, Mockito.never()).save(any());
    }

    @Test
    void testTakeTask_ExecutorNotExecutor() {
        Long taskId = 1L;
        Long executorId = 2L;

        UserEntity author = new UserEntity();
        author.setId(10L);
        author.setRole(Role.AUTHOR);

        UserEntity executor = new UserEntity();
        executor.setId(executorId);
        executor.setRole(Role.AUTHOR); // НЕ EXECUTOR

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(author);

        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author);
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        Mockito.when(userRepository.findById(executorId)).thenReturn(Optional.of(executor));

        assertThrows(AccessDeniedException.class, () -> taskService.takeTask(taskId, executorId));
    }
    @Test
    void testGetTask_Success() {
        Long taskId = 1L;
        TaskEntity task = new TaskEntity();
        task.setId(taskId);

        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Optional<TaskEntity> result = taskService.getTask(taskId);

        assertTrue(result.isPresent());
        assertEquals(taskId, result.get().getId());
    }

    @Test
    void testGetTask_NotFound() {
        Long taskId = 1L;

        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.getTask(taskId));
    }
    @Test
    void testRemoveTask_Success() {
        Long taskId = 1L;
        UserEntity author = new UserEntity();
        author.setId(10L);

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(author);

        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author);
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.removeTask(taskId);

        Mockito.verify(taskRepository, Mockito.times(1)).delete(task);
    }

    @Test
    void testRemoveTask_UserNotAuthenticated() {
        Mockito.when(userService.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> taskService.removeTask(1L));
    }

    @Test
    void testRemoveTask_TaskNotFound() {
        Mockito.when(userService.getAuthenticatedUser()).thenReturn(new UserEntity());
        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.removeTask(1L));
    }

    @Test
    void testRemoveTask_AccessDenied() {
        Long taskId = 1L;
        UserEntity author = new UserEntity();
        author.setId(10L);

        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(20L);

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(anotherUser);

        Mockito.when(userService.getAuthenticatedUser()).thenReturn(author);
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.removeTask(taskId));
    }
    @Test
    void testUpdateTask_Success() {
        Long taskId = 1L;
        UserEntity author = new UserEntity();
        author.setId(10L);
        author.setRole(Role.AUTHOR);

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setUser(author);

        CreateOrUpdateTaskDTO dto = new CreateOrUpdateTaskDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated description");
        dto.setStatus(TaskStatus.IN_PROGRESS);

        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        Mockito.when(taskRepository.save(any(TaskEntity.class))).thenReturn(task);
        Mockito.when(taskMapper.taskDTO(any(TaskEntity.class))).thenReturn(new TaskDTO());

        TaskDTO result = taskService.updateTask(taskId, dto, author);

        assertNotNull(result);
        assertEquals("Updated title", task.getTitle());
        assertEquals("Updated description", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void testUpdateTask_NotFound() {
        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, new CreateOrUpdateTaskDTO(), new UserEntity()));
    }

    @Test
    void testUpdateTask_NoRights() {
        UserEntity user = new UserEntity();
        user.setRole(Role.EXECUTOR);

        TaskEntity task = new TaskEntity();
        task.setId(1L);

        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(NoRightsException.class, () -> taskService.updateTask(1L, new CreateOrUpdateTaskDTO(), user));
    }

    @Test
    void testAddCommentToTask_Success() {
        Long taskId = 1L;
        String userEmail = "user@example.com";

        TaskEntity task = new TaskEntity();
        task.setId(taskId);

        UserEntity user = new UserEntity();
        user.setEmail(userEmail);

        CommentRequest request = new CommentRequest();
        request.setUserEmail(userEmail);
        request.setText("New comment");

        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(user);
        Mockito.when(commentRepository.save(any(CommentEntity.class))).thenReturn(new CommentEntity());
        Mockito.when(commentMapper.toDTO(any(CommentEntity.class))).thenReturn(new CommentDTO());

        CommentDTO result = taskService.addCommentToTask(taskId, request);

        assertNotNull(result);
    }

    @Test
    void testAddCommentToTask_TaskNotFound() {
        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.addCommentToTask(1L, new CommentRequest()));
    }

    @Test
    void testAddCommentToTask_UserNotFound() {
        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.of(new TaskEntity()));
        Mockito.when(userRepository.findByEmail("user@example.com")).thenReturn(null);

        CommentRequest request = new CommentRequest();
        request.setUserEmail("user@example.com");

        assertThrows(UsernameNotFoundException.class, () -> taskService.addCommentToTask(1L, request));
    }
    @Test
    void testUpdateTaskPriority_Success() {
        TaskEntity task = new TaskEntity();
        task.setId(1L);

        UserEntity user = new UserEntity();
        user.setRole(Role.AUTHOR);

        UpdateTaskPriorityDTO dto = new UpdateTaskPriorityDTO();
        dto.setPriority(TaskPriority.HIGH);

        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.updateTaskPriority(1L, dto, user);

        assertEquals(TaskPriority.HIGH, task.getPriority());
    }

    @Test
    void testUpdateTaskPriority_NoRights() {
        UserEntity user = new UserEntity();
        user.setRole(Role.EXECUTOR);

        assertThrows(AccessDeniedException.class, () -> taskService.updateTaskPriority(1L, new UpdateTaskPriorityDTO(), user));
    }

}