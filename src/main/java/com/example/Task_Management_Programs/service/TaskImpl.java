package com.example.Task_Management_Programs.service;


import com.example.Task_Management_Programs.claass.CommentRequest;
import com.example.Task_Management_Programs.claass.CreateOrUpdateTaskDTO;
import com.example.Task_Management_Programs.claass.UpdateTaskPriorityDTO;
import com.example.Task_Management_Programs.claass.UpdateTaskStatusDTO;
import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.dto.TaskDTO;
import com.example.Task_Management_Programs.entity.CommentEntity;
import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.enumm.Role;
import com.example.Task_Management_Programs.exception.NoRights;
import com.example.Task_Management_Programs.exception.ResourceNotFoundException;
import com.example.Task_Management_Programs.exception.TaskNotFoundException;
import com.example.Task_Management_Programs.impl.TaskService;
import com.example.Task_Management_Programs.impl.UserService;
import com.example.Task_Management_Programs.mapper.TaskMapper;
import com.example.Task_Management_Programs.mapper.UserMapper;
import com.example.Task_Management_Programs.mapperr.CommentMapperr;
import com.example.Task_Management_Programs.mapperr.TaskMapperr;
import com.example.Task_Management_Programs.repository.CommentRepository;
import com.example.Task_Management_Programs.repository.TaskRepository;
import com.example.Task_Management_Programs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления задачами.
 *
 * <p>Этот класс предоставляет реализацию интерфейса {@link TaskService} для работы с задачами,
 * включая создание, обновление, удаление, получение задач и управление их приоритетами, статусами и комментариями.</p>
 *
 * Основные функции:
 * - Создание новых задач пользователями с ролью `AUTHOR`.
 * - Назначение задач пользователям с ролью `EXECUTOR`.
 * - Обновление задач (заголовок, описание, статус) в зависимости от роли текущего пользователя.
 * - Удаление задач с проверкой прав доступа.
 * - Управление комментариями к задачам (добавление, обновление, удаление).
 *
 * Используемые зависимости:
 * - {@link TaskRepository} — для работы с задачами в базе данных.
 * - {@link TaskMapper} и {@link TaskMapperr} — для преобразования сущностей задач в DTO и обратно.
 * - {@link UserService} — для получения данных текущего пользователя.
 * - {@link UserRepository} — для работы с пользователями в базе данных.
 * - {@link UserMapper} — для преобразования сущностей пользователей в DTO.
 * - {@link CommentRepository} — для работы с комментариями в базе данных.
 * - {@link CommentMapperr} — для преобразования сущностей комментариев в DTO.
 *
 * <p>Методы класса обеспечивают строгую проверку прав доступа, основанную на ролях пользователей,
 * что предотвращает выполнение несанкционированных операций.</p>
 */
@Service
public class TaskImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskImpl.class);
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskMapperr taskMapperr;
    private final CommentRepository commentRepository;
    private final CommentMapperr commentMapperr;


    @Autowired
    public TaskImpl(TaskRepository taskRepository, TaskMapper taskMapper, UserService userService, UserRepository userRepository, UserMapper userMapper, TaskMapperr taskMapperr, CommentRepository commentRepository, CommentMapperr commentMapperr) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.taskMapperr = taskMapperr;
        this.commentRepository = commentRepository;
        this.commentMapperr = commentMapperr;
    }
    /**
     * Получает список задач по идентификатору пользователя.
     * @param userId Идентификатор пользователя.
     * @return Список задач в формате DTO, связанных с пользователем.
     */
    @Override
    public List<TaskDTO> getTasksByUserId(Long userId) {
        List<TaskEntity> tasks = taskRepository.findByUserId(userId);
        return tasks.stream().map(taskMapperr::taskDTO).collect(Collectors.toList());
    }

    /**
     * Добавляет новую задачу для пользователя с указанным ID.
     * @param properties Данные задачи для создания (DTO).
     * @param id Идентификатор пользователя.
     * @return DTO созданной задачи.
     * @throws AccessDeniedException Если у пользователя нет роли AUTHOR.
     */
    @Override
    public TaskDTO addTask(CreateOrUpdateTaskDTO properties, Long id) {
        UserEntity user = userService.findById(id);
        // Проверка, что у пользователя роль AUTHOR
        if (user.getRole() != Role.AUTHOR) {
            throw new AccessDeniedException("У вас нет роли АВТОРА для создания задачи");
        }
        TaskEntity taskEntity = taskMapperr.createOrUpdateAdToAd(properties, user);
        TaskEntity savedTaskEntity = taskRepository.save(taskEntity);
        return taskMapperr.taskDTO(savedTaskEntity);

    }
    /**
     * Назначает задачу пользователю-исполнителю.
     * @param taskId Идентификатор задачи.
     * @param userId Идентификатор пользователя-исполнителя.
     * @throws AccessDeniedException Если текущий пользователь не является автором.
     * @throws EntityNotFoundException Если задача или пользователь не найдены.
     * @throws IllegalArgumentException Если пользователь-исполнитель не имеет роли EXECUTOR.
     */
    @Override
    public void takeTask(Long taskId, Long userId) {
        // Получение текущего аутентифицированного пользователя
        UserEntity user = userService.getAuthenticatedUser();
        // Проверка роли пользователя
        if (!user.getRole().equals(Role.AUTHOR)) {
            throw new AccessDeniedException("Только пользователи с ролью 'AUTHOR' могут назначать задачи");
        }
        // Поиск задачи по ID
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
        // Проверка, что текущий пользователь создал эту задачу
        if (!taskEntity.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Вы можете назначать исполнителей только для своих задач");
        }
        // Поиск пользователя-исполнителя
        UserEntity executorEntity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        // Проверка роли пользователя-исполнителя
        if (!executorEntity.getRole().equals(Role.EXECUTOR)) {
            throw new IllegalArgumentException("Пользователю нужно иметь роль 'EXECUTOR', чтобы быть назначенным исполнителем задачи");
        }
        // Назначение пользователя-исполнителя для задачи
        taskEntity.setUserExecutor(executorEntity);
        // Обновление поля currentTask у пользователя-исполнителя
        executorEntity.setCurrentTask(taskEntity);
        // Сохранение изменений в репозитории
        taskRepository.save(taskEntity);
        userRepository.save(executorEntity);
    }

    /**
     * Получает задачу по ID.
     * @param id Идентификатор задачи.
     * @return Опциональный объект задачи.
     * @throws ResourceNotFoundException Если задача не найдена.
     */
    @Override
    public Optional<TaskEntity> getTask(Long id) {
        return Optional.ofNullable(taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found")));
    }

    /**
     * Удаляет задачу по ID.
     * @param id Идентификатор задачи.
     * @throws AccessDeniedException Если пользователь не авторизован или не является владельцем задачи.
     * @throws TaskNotFoundException Если задача с указанным ID не найдена.
     */
    @Override
    @Transactional
    public void removeTask(Long id) {
        // Получение текущего аутентифицированного пользователя
        UserEntity currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Пользователь не аутентифицирован");
        }

        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + id + " не найдена"));

        // Проверка, что пользователь может удалить только свою задачу
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не имеете прав на удаление этой задачи");
        }

        taskRepository.delete(task);
    }
    /**
     * Обновляет задачу на основе данных из DTO.
     * @param id Идентификатор задачи.
     * @param createOrUpdateTaskDTO DTO с обновляемыми данными задачи.
     * @param currentUser Текущий аутентифицированный пользователь.
     * @return DTO обновленной задачи.
     * @throws NoRights Если пользователь не имеет прав на обновление задачи.
     * @throws TaskNotFoundException Если задача не найдена.
     */
    @Override
    public TaskDTO updateTask(Long id, CreateOrUpdateTaskDTO createOrUpdateTaskDTO, UserEntity currentUser) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + id));
        // Осуществляю обновление поля на основе роли текущего пользователя
        if (currentUser.getRole() == Role.AUTHOR) {
            if (createOrUpdateTaskDTO.getTitle() != null) {
                task.setTitle(createOrUpdateTaskDTO.getTitle());
            }
            if (createOrUpdateTaskDTO.getDescription() != null) {
                task.setDescription(createOrUpdateTaskDTO.getDescription());
            }
            if (createOrUpdateTaskDTO.getStatus() != null) {
                task.setStatus(createOrUpdateTaskDTO.getStatus());
            }
        } else {
            throw new NoRights("У пользователя нет прав для обновления этой задачи");
        }
        TaskEntity updatedTask = taskRepository.save(task);
        return taskMapper.taskToTaskDTO(updatedTask);
    }
    /**
     * Добавляет комментарий к задаче.
     * @param taskId Идентификатор задачи.
     * @param commentRequest DTO с текстом комментария и email пользователя.
     * @return DTO добавленного комментария.
     * @throws EntityNotFoundException Если задача или пользователь не найдены.
     */
    @Override
    public CommentDTO addCommentToTask(Long taskId, CommentRequest commentRequest) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        UserEntity user = userRepository.findByEmail(commentRequest.getUserEmail());
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + commentRequest.getUserEmail());
        }

        CommentEntity comment = new CommentEntity();
        comment.setTask(task);
        comment.setUserId(user);
        comment.setText(commentRequest.getText());
        System.out.println("Comment text: " + commentRequest.getText());
        comment.setСreatedAt(LocalDateTime.now());

        CommentEntity savedComment = commentRepository.save(comment);

        // Преобразуем сохраненный комментарий в DTO

        return commentMapperr.toDTO(savedComment);
    }
    /**
     * Обновляет приоритет задачи.
     * @param taskId Идентификатор задачи.
     * @param updateDto DTO с новым приоритетом задачи.
     * @param user Текущий аутентифицированный пользователь.
     * @throws AccessDeniedException Если пользователь не имеет роли AUTHOR.
     * @throws Exception Если задача не найдена.
     */
    @Override
    @Transactional
    public void updateTaskPriority(Long taskId, UpdateTaskPriorityDTO updateDto, UserEntity user) {
        if (user.getRole() != Role.AUTHOR) {
            throw new AccessDeniedException("У пользователя нет разрешения на обновление приоритета задачи");
        }
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setPriority(updateDto.getPriority());
        taskRepository.save(task);
    }
    /**
     * Обновляет статус задачи.
     * @param taskId Идентификатор задачи.
     * @param updateDto DTO с новым статусом задачи.
     * @param user Текущий аутентифицированный пользователь.
     * @throws AccessDeniedException Если пользователь не имеет роли EXECUTOR.
     * @throws Exception Если задача не найдена.
     */
    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, UpdateTaskStatusDTO updateDto, UserEntity user) {
        if (user.getRole() != Role.EXECUTOR) {
            throw new AccessDeniedException("У пользователя нет разрешения на обновление статуса задачи");
        }
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(updateDto.getStatus());
        taskRepository.save(task);
    }
}
