package com.example.Task_Management_Programs.repository;


import com.example.Task_Management_Programs.entity.TaskEntity;
import com.example.Task_Management_Programs.enums.TaskPriority;
import com.example.Task_Management_Programs.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);
    Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);
    @Query("SELECT t FROM TaskEntity t WHERE "
            + "(:status IS NULL OR t.status = :status) AND "
            + "(:priority IS NULL OR t.priority = :priority)")
    Page<TaskEntity> findByFilters(@Param("status") TaskStatus status,
                                   @Param("priority") TaskPriority priority,
                                   Pageable pageable);
    Page<TaskEntity> findByUserExecutorId(Long executorId, Pageable pageable);


}
