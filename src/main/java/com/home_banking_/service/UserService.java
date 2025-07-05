package com.home_banking_.service;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import jakarta.validation.Valid;

import java.util.List;


public interface UserService {

    UserResponseDto findById(Long id);

    UserResponseDto findByEmail(String email);

    UserResponseDto createUser(UserRequestDto dto);

    List<UserResponseDto> findAll();

    UserResponseDto save(Long id , @Valid UserResponseDto dto);

    void deleteById(Long id);
}
