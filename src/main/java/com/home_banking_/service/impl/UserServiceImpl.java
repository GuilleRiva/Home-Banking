package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.UsersMapper;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public UserServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
    }

    @Override
    public UserResponseDto findById(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with ID:" + id));

        return usersMapper.toDTO(user);
    }

    @Override
    public UserResponseDto findByEmail(String email) {
        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email:" + email));

        return usersMapper.toDTO(users);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto dto) {
        Users users = usersMapper.toEntity(dto);
        users.setRegistrationDate(LocalDateTime.now());
        Users savedUser = usersRepository.save(users);

        return usersMapper.toDTO(savedUser);
    }


    @Override
    public List<UserResponseDto> findAll() {
        return usersRepository.findAll().stream()
                .map(usersMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto save(Long id, UserRequestDto dto) {
        Users existingUser = usersRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with ID:" + id));

        existingUser.setName(dto.getName());
        existingUser.setSurname(dto.getSurname());
        existingUser.setEmail(dto.getEmail());
        existingUser.setPassword(dto.getPassword());
        existingUser.setRol(Rol.valueOf(dto.getRol()));

        Users updateUser = usersRepository.save(existingUser);
        return usersMapper.toDTO(updateUser);
    }

    @Override
    public void deleteById(Long id) {

        if (!usersRepository.existsById(id)){
            throw new ResourceNotFoundException("user not found with ID: " + id);
        }
        usersRepository.deleteById(id);
    }
}
