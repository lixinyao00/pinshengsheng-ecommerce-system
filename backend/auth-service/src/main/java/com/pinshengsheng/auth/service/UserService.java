package com.pinshengsheng.auth.service;

import com.pinshengsheng.auth.dto.RegisterRequest;
import com.pinshengsheng.auth.model.UserAccount;

public interface UserService {

    UserAccount findByUsername(String username);

    UserAccount findById(Long id);

    UserAccount register(RegisterRequest request);

    boolean matchesPassword(String rawPassword, String passwordHash);
}
