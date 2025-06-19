package com.home_banking_.service;

import com.home_banking_.model.Users;

import java.util.List;

public interface UserService {

    Users findById(Long id);

    Users findByEmail(String email);

    List<Users> findAll();

    Users save(Users users);

    void deleteById(Long id);
}
