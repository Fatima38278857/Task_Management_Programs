package com.example.Task_Management_Programs.claass;


import com.example.Task_Management_Programs.enumm.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class UserProfileRequest{
    @JsonProperty("email")
    private String email;
    @JsonProperty("role")
    private Role role;

    public UserProfileRequest() {
    }

    public UserProfileRequest(String email, Role role) {
        this.email = email;
        this.role = role;
    }

}
