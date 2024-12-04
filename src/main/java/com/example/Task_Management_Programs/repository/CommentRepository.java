package com.example.Task_Management_Programs.repository;


import com.example.Task_Management_Programs.entity.CommentEntity;
import com.example.Task_Management_Programs.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteCommentByTaskIdAndId(long task_Id, long comments_Id);

    @Query("SELECT c FROM CommentEntity c WHERE c.task.id = :taskId AND c.userId.id = :authorId AND c.id = :commentsId")
    List<CommentEntity> findComments(@Param("taskId") Long taskId,
                                     @Param("authorId") Long authorId,
                                     @Param("commentsId") Long commentsId);

    List<CommentEntity> findByTask(TaskEntity task);

    List<CommentEntity> findByTaskId(Long taskId);
}


