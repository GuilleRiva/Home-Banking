package com.home_banking_.controllers;

import com.home_banking_.model.Users;
import com.home_banking_.service.UserService;
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
    public ResponseEntity<List<Users>> getAllUsers(){
        List<Users> users = userService.findAll();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id){
        Users users = userService.findById(id);
        return ResponseEntity.ok(users);

    }


    @GetMapping("/{email}")
    public ResponseEntity<Users> getByEmail(@PathVariable String email){
        Users users= userService.findByEmail(email);
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users newUser){
        Users created = userService.createUser(newUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @PostMapping("/{userId}")
    public ResponseEntity<Users> updateUser(@PathVariable Long userId,
                                            @RequestBody Users updatedUser){

        Users users = userService.save(userId, updatedUser);
        return ResponseEntity.ok(users);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
