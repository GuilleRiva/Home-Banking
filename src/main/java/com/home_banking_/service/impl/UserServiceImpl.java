package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.UsersMapper;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public UserResponseDto findById(Long id) {
        log.info("Searching user by ID: {}", id);

        Users user = usersRepository.findById(id)
                .orElseThrow(()-> {
                    log.warn("User not found. ID: {}", id);
                   return new ResourceNotFoundException("User not found with ID:" + id);
                });

        log.info("User found. ID: {}", id);
        return usersMapper.toDTO(user);
    }

    @Override
    public UserResponseDto findByEmail(String email) {
        log.info("Searching user for email: {}",email);

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> {
                    log.warn("User not found with email: {}", email);
                   return new ResourceNotFoundException("User not found with email:" + email);
                });

        log.info("User found with email: {}", email);
        return usersMapper.toDTO(users);
    }



    @Override
    public UserResponseDto createUser(UserRequestDto dto) {
        log.info("Creating new user with email: {}",dto.getEmail());

        Users users = usersMapper.toEntity(dto);
        users.setRegistrationDate(LocalDateTime.now());
        Users savedUser = usersRepository.save(users);

        log.info("User created successfully. ID: {} | Email: {}", savedUser.getId(), savedUser.getEmail());
        return usersMapper.toDTO(savedUser);
    }



    @Override
    public List<UserResponseDto> findAll() {
        log.info("Querying all registered users");

        List<UserResponseDto> result = usersRepository.findAll().stream()
                .map(usersMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Total users found: {}", result.size());
        return result;
    }



    @Override
    public UserResponseDto save(Long id, @Valid UserResponseDto dto) {
        log.info("Updating user data ID: {}", id)
        ;
        Users existingUser = usersRepository.findById(id)
                .orElseThrow(()-> {
                            log.warn("User not found to update. ID: {}", id);
                           return new ResourceNotFoundException("User not found with ID:" + id);
                        });

        existingUser.setName(dto.getName());
        existingUser.setSurname(dto.getSurname());
        existingUser.setEmail(dto.getEmail());
        existingUser.setPassword(dto.getPassword());
        existingUser.setRol(Rol.valueOf(String.valueOf(dto
                .getRol())));

        Users updateUser = usersRepository.save(existingUser);

        log.info("User successfully updated. ID: {}", id);
        return usersMapper.toDTO(updateUser);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Requesting deletion of user ID: {}", id);

        if (!usersRepository.existsById(id)){
            log.warn("Attempt to delete non-existent user. ID: {}", id);
            throw new ResourceNotFoundException("user not found with ID: " + id);
        }
        usersRepository.deleteById(id);
        log.info("User successfully deleted. ID: {}", id);
    }
}
