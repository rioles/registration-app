package com.videostream.registration.service;

import com.videostream.registration.exception.UserAlreadyExistsInKeycloakException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createKeycloakUser(String email, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRequiredActions(List.of("VERIFY_EMAIL", "CONFIGURE_TOTP"));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() == 409) {
            throw new UserAlreadyExistsInKeycloakException("Username ou email déjà existant");
        }
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
        }

        // 1. Récupérer l'ID Keycloak
        String keycloakId = CreatedResponseUtil.getCreatedId(response);

        // 2. Envoyer l'email de vérification immédiatement après création
        keycloak.realm(realm)
                .users()
                .get(keycloakId)
                .sendVerifyEmail();

        return keycloakId;
    }
}