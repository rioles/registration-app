package com.videostream.registration.service.impl;

import com.videostream.registration.dto.RegisterRequest;
import com.videostream.registration.dto.UserResponse;
import com.videostream.registration.exception.EmailAlreadyExistsException;
import com.videostream.registration.exception.UserNotFoundException;
import com.videostream.registration.mapper.UserMapper;
import com.videostream.registration.model.entity.User;
import com.videostream.registration.repository.UserRepository;
import com.videostream.registration.service.KeycloakService;
import com.videostream.registration.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Vérifier en BD
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // 2. Créer dans Keycloak EN PREMIER
        String keycloakId = keycloakService.createKeycloakUser(request.getEmail(), request.getPassword());

        // 3. Vérifier que keycloakId est valide avant de sauvegarder
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new RuntimeException("Keycloak user creation failed: no ID returned");
        }

        // 4. Sauvegarder en BD seulement si Keycloak a réussi
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .email(request.getEmail())
                .keycloakId(keycloakId)
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
}
