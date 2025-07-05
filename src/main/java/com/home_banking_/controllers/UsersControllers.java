package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import com.home_banking_.model.Users;
import com.home_banking_.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersControllers {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = userService.findAll();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id){
        UserResponseDto users = userService.findById(id);
        return ResponseEntity.ok(users);

    }


    @GetMapping("/{email}")
    public ResponseEntity<UserResponseDto> getByEmail(@PathVariable String email){
        UserResponseDto users= userService.findByEmail(email);
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto newUser){
        UserResponseDto created = userService.createUser(newUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @PostMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId,
                                            @Valid @RequestBody UserResponseDto updatedUser){

        UserResponseDto userResponse = userService.save(userId,updatedUser);
        return ResponseEntity.ok(userResponse);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
