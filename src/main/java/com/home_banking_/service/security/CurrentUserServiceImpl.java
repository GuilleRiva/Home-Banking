package com.home_banking_.service.security;

import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService{

    private final UsersRepository usersRepository;

    public CurrentUserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        return authentication.getName();
    }

    @Override
    public Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"))
                .getId();
    }

    @Override
    public Users getCurrentUser() {
        String email = getCurrentUserEmail();
        return usersRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("Authenticated user not found"));
    }
}
