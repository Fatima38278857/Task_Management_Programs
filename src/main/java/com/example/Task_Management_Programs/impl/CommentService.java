package com.example.Task_Management_Programs.impl;



import com.example.Task_Management_Programs.claass.CreateOrUpdateComment;
import com.example.Task_Management_Programs.dto.CommentDTO;
import com.example.Task_Management_Programs.entity.CommentEntity;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getAllComment(long taskId);


    void deleteComment(long task_Id, long comment_Id);
    CommentEntity saveComment(CommentEntity comment);
    CommentDTO updateCommentId(long task_Id, long comment_Id, CreateOrUpdateComment createOrUpdateComment);

}