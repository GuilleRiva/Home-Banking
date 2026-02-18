package com.home_banking_.service.impl;

import com.home_banking_.dto.request.UserRequestDto;
import com.home_banking_.dto.response.UserResponseDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.UsersMapper;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public UserServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {

        Users user = usersRepository.findById(id)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "User not found with ID:" + id
                ));

        return usersMapper.toDTO(user);
    }


    @Override
    public UserResponseDto findByEmail(String email) {

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found with email:" + email
                ));

        return usersMapper.toDTO(users);
    }



    @Override
    public UserResponseDto createUser(UserRequestDto dto) {

        Users users = usersMapper.toEntity(dto);
        users.setRegistrationDate(LocalDateTime.now());
        Users savedUser = usersRepository.save(users);

        log.info("User created successfully. ID: {} | Email: {}", savedUser.getId(), savedUser.getEmail());
        return usersMapper.toDTO(savedUser);
    }



    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return usersRepository.findAll().stream()
                .map(u -> new UserResponseDto(
                        u.getId(),
                        u.getName(),
                        u.getSurname(),
                        u.getEmail(),
                        u.getRegistrationDate(),
                        u.getRol()
                ))
                .toList();
    }



    @Override
    public UserResponseDto save(Long id, @Valid UserResponseDto dto) {

        Users existingUser = usersRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found with ID:" + id
                ));

        existingUser.setName(dto.getName());
        existingUser.setSurname(dto.getSurname());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRol(Rol.valueOf(String.valueOf(dto
                .getRol())));

        Users updateUser = usersRepository.save(existingUser);

        log.info("User successfully updated. ID: {}", id);
        return usersMapper.toDTO(updateUser);
    }

    @Override
    public void deleteById(Long id) {

        if (!usersRepository.existsById(id)){
            log.warn("Attempt to delete non-existent user. ID: {}", id);
            throw new ResourceNotFoundException("user not found with ID: " + id);
        }
        usersRepository.deleteById(id);
        log.info("User successfully deleted. ID: {}", id);
    }
}
