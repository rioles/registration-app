package com.videostream.registration.service;

import com.videostream.registration.dto.RegisterRequest;
import com.videostream.registration.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse findById(UUID id);
    List<UserResponse> findAll();
}
