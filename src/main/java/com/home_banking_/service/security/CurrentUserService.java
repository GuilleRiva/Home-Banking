package com.home_banking_.service.security;

import com.home_banking_.model.Users;

public interface CurrentUserService {
    String getCurrentUserEmail();
    Long getCurrentUserId();
    Users getCurrentUser();
}
