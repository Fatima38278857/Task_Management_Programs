package com.example.Task_Management_Programs.enumm;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Role {
    AUTHOR, EXECUTOR
}
