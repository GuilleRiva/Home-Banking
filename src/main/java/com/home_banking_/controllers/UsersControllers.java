package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import com.home_banking_.model.Users;
import com.home_banking_.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "User Controller", description = "Operations related to user management and registration ")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersControllers {

    private final UserService userService;



    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users registered in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        log.info("GET /api/users - Requesting list of all users");

        List<UserResponseDto> users = userService.findAll();
        log.info("Recovered users: {}", users.size());

        return ResponseEntity.ok(users);
    }


    @Operation(
            summary = "Get user by ID",
            description = "Retrieves the user information based on the provided user ID"
    )
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(name = "userId", description = "Unique identifier of the user", required = true)
            @PathVariable Long id){

        log.info("GET /api/users/{} - Searching for user by ID", id);

        UserResponseDto users = userService.findById(id);
        log.info("User not found: {}", users.getEmail());
        return ResponseEntity.ok(users);

    }


    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<UserResponseDto> getByEmail(@PathVariable String email){
        log.info("GET /api/users/email/{} - Searching for user by email",email);

        UserResponseDto users= userService.findByEmail(email);
        log.info("User recovered by email: {}", users.getEmail());
        return ResponseEntity.ok(users);
    }



    @Operation(
            summary = "Register a new user",
            description = "Creates a new user in the system based on the provided data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user request")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto newUser){
        log.info("POST /api/users - Registering new user: {}", newUser.getEmail());

        UserResponseDto created = userService.createUser(newUser);
        log.info("User created successfully with ID: {}", created.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @PostMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId,
                                            @Valid @RequestBody UserResponseDto updatedUser){

        log.info("POST /api/users/{} - Updating user", userId);

        UserResponseDto userResponse = userService.save(userId,updatedUser);
        log.info("User successfully updated: {}", userResponse.getEmail());
        return ResponseEntity.ok(userResponse);
    }



    @Operation(
            summary = "Delete user by ID",
            description = "Deletes the user identified by the provided ID from the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(name = "userId", description = "Unique identifier of the user", required = true)
            @PathVariable Long userId){

        log.warn("DELETE /api/users/{} - Requesting user deletion", userId);

        userService.deleteById(userId);
        log.info("User with ID {} successfully deleted", userId);
        return ResponseEntity.noContent().build();
    }
}
