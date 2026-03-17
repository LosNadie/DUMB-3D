package com.dumb.service;

import com.dumb.dto.request.LoginRequest;
import com.dumb.dto.request.RegisterRequest;
import com.dumb.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
