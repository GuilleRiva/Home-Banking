package com.home_banking_.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.UserRequestDto;
import com.home_banking_.dto.response.UserProfileResponseDto;
import com.home_banking_.dto.response.UserResponseDto;
import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.enums.Rol;
import com.home_banking_.enums.UserStatus;
import com.home_banking_.exceptions.custom.AccountStateException;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.UsersMapper;
import com.home_banking_.model.IdempotencyRecord;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.UserService;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final CurrentUserService currentUserService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper, CurrentUserService currentUserService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.currentUserService = currentUserService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        Users user = getUserByIdOrThrow(id);
        return usersMapper.toUserResponseDto(user);
    }


    @Override
    public UserResponseDto findByEmail(String email) {

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));

        return usersMapper.toUserResponseDto(users);
    }


    @Transactional
    @Override
    public UserProfileResponseDto createUser(String idempotencyKey,UserRequestDto dto) {
        Long userId = currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult =
                idempotencyService.validateAndRegister(
                        idempotencyKey,
                        userId,
                        IdempotencyOperation.CREATE_USER,
                        dto
                );

        if (validationResult.isReplay()) {
            log.info("[CREATE_USER_IDEMPOTENT_REPLAY] userId={} idempotencyKey={}", userId, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        UserProfileResponseDto responseDto = idempotencyService.executeAndComplete(
                validationResult.getRecord(),
                ()-> executeCreateUser(dto),
                UserProfileResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during create user"
        );

        log.info("[CREATE_USER_IDEMPOTENT_COMPLETED] userId={} createdUserId={} recordId={}",
                userId,responseDto.getId(),validationResult.getRecord().getId());

        return responseDto;
    }

    private UserProfileResponseDto executeCreateUser(UserRequestDto dto) {
        Users users = usersMapper.toEntity(dto);
        users.setRegistrationDate(LocalDateTime.now());

        Users savedUser = usersRepository.save(users);

        log.info("[CREATE_USER_SUCCESS] userId={} email={}", savedUser.getId(), savedUser.getEmail());
        return usersMapper.toUserProfileResponseDto(savedUser);
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return usersRepository.findAll().stream()
                .map(usersMapper::toUserResponseDto)
                .toList();
    }


    @Override
    public UserProfileResponseDto save(Long id, @Valid UserResponseDto dto) {

        Users existingUser = getUserByIdOrThrow(id);

        existingUser.setName(dto.getName());
        existingUser.setSurname(dto.getSurname());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRol(Rol.valueOf(String.valueOf(dto
                .getRol())));

        Users updateUser = usersRepository.save(existingUser);

        log.info("User successfully updated. ID: {}", id);
        return usersMapper.toUserProfileResponseDto(updateUser);
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

    @Transactional
    public UserResponseDto activateUser(Long userId) {
        Users users = getUserByIdOrThrow(userId);

        if (users.getUserStatus() == UserStatus.ACTIVE) {
            throw new BusinessException("User is already active");
        }

        if (users.getUserStatus() == UserStatus.BLOCKED) {
            throw new AccountStateException("Blocked users cannot be activated directly");
        }

        users.setUserStatus(UserStatus.ACTIVE);
        Users savedUser= usersRepository.save(users);

        log.info("[USER_ACTIVATED] userId={}", savedUser.getId());

        return usersMapper.toUserResponseDto(savedUser);
    }

    private UserProfileResponseDto replayResponse(IdempotencyRecord record) {
        try {
            return objectMapper.readValue(record.getResponseBody(), UserProfileResponseDto.class);
        } catch (Exception e) {
            throw new BusinessException("Couldn't replay idempotent create user response");
        }
    }

    private Users getUserByIdOrThrow(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with ID: " + id));
    }
}
