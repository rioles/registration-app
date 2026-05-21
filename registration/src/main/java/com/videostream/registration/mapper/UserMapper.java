package com.videostream.registration.mapper;

import com.videostream.registration.dto.RegisterRequest;
import com.videostream.registration.dto.UserResponse;
import com.videostream.registration.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .email(request.getEmail())
                .build();
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .age(user.getAge())
                .email(user.getEmail())
                .keycloakId(user.getKeycloakId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
