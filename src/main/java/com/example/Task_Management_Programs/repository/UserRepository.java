package com.example.Task_Management_Programs.repository;


import com.example.Task_Management_Programs.entity.Register;
import com.example.Task_Management_Programs.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

     UserEntity findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByRegister(Register register);

    Optional<UserEntity> findById(Long id);

    UserEntity findByEmail(String email);
    UserEntity findByRegisterUsername(String username);
    boolean existsByEmail(String email);

}



