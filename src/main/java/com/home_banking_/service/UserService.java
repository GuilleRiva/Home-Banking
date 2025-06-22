package com.home_banking_.service;

import com.home_banking_.model.Users;

import java.util.List;


public interface UserService {

    Users findById(Long id);

    Users findByEmail(String email);

    Users createUser(Users user);

    List<Users> findAll();

    Users save(Long id , Users users);

    void deleteById(Long id);
}
