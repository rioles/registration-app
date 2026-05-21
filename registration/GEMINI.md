# Project: Video Stream Registration Service

## Context
This is a Spring Boot 4.0.6 REST API backend for a video streaming platform.
The service handles user registration and profile management.
The API stores user data in a local PostgreSQL database.
Java version: 21.
No security configuration for now, focus only on the registration feature.

---

## Architecture Pattern
Strict MVC pattern with the following rules:
- The Controller layer must depend ONLY on the Service interface, never on the implementation directly
- The Service layer must use DTOs to communicate with the Controller layer
- The Repository layer must use the Entity only, never DTOs
- The Service implementation is responsible for mapping between Entity and DTO
- This is the DTO flow to strictly follow:

```
Controller
    ↓ receives RegisterRequest (DTO)
    ↓ calls UserService interface
Service Interface
    ↓ implemented by UserServiceImpl
    ↓ maps RegisterRequest → User entity
    ↓ calls UserRepository
Repository
    ↓ saves User entity
    ↓ returns User entity to ServiceImpl
ServiceImpl
    ↓ maps User entity → UserResponse (DTO)
    ↓ returns UserResponse to Controller
Controller
    ↓ returns UserResponse to client
```

---

## Base Package
com.videostream.registration

---

## Package Structure to Generate

- controller → UserController
- service → UserService (interface only)
- service.impl → UserServiceImpl (implements UserService)
- repository → UserRepository
- model.entity → User
- dto → RegisterRequest, UserResponse
- exception → GlobalExceptionHandler, EmailAlreadyExistsException, UserNotFoundException
- mapper → UserMapper (handles mapping between User entity and DTOs)

---

## Dependencies to ADD in pom.xml
The following dependencies are missing and must be added:
- spring-boot-starter-validation

---

## Entity: User
Table name: users
Fields:
- id: UUID, primary key, auto generated
- firstName: String, not null
- lastName: String, not null
- age: Integer, not null
- email: String, not null, unique
- createdAt: LocalDateTime, automatically set on creation

---

## DTO: RegisterRequest
Fields with validation annotations:
- firstName: not blank
- lastName: not blank
- age: not null, min 1, max 120
- email: not blank, valid email format
- password: not blank, minimum 8 characters

---

## DTO: UserResponse
Fields:
- id
- firstName
- lastName
- age
- email
- createdAt

---

## Mapper: UserMapper
Create a UserMapper class annotated with @Component.
It must have the following methods:
- toEntity(RegisterRequest request) → converts RegisterRequest to User entity
- toResponse(User user) → converts User entity to UserResponse DTO
The mapper must never be called from the Controller, only from the ServiceImpl.

---

## Service Interface: UserService
The interface must declare the following methods:
- register(RegisterRequest request) → returns UserResponse
- findById(UUID id) → returns UserResponse
- findAll() → returns List of UserResponse

---

## Service Implementation: UserServiceImpl
Implements UserService interface.
Must be annotated with @Service.
Must inject UserRepository and UserMapper.
For the register method:
- Check if email already exists in DB, throw EmailAlreadyExistsException if true
- Use UserMapper to convert RegisterRequest to User entity
- Set createdAt to current date and time
- Save the entity via UserRepository
- Use UserMapper to convert saved User entity to UserResponse
- Return UserResponse

---

## Repository: UserRepository
Extends JpaRepository with User entity and UUID as ID type.
Must have the following custom methods:
- existsByEmail(String email) → returns boolean
- findByEmail(String email) → returns Optional of User

---

## Controller: UserController
Must inject UserService interface (NOT UserServiceImpl directly).
Base mapping: /api/v1/users
Endpoints:
- POST /register → public, accepts RegisterRequest with @Valid, returns UserResponse with 201 status
- GET /{id} → returns UserResponse by UUID with 200 status
- GET / → returns list of all UserResponse with 200 status

---

## Exception Handling
Create a GlobalExceptionHandler with @RestControllerAdvice.
Handle the following cases:
- EmailAlreadyExistsException → 409 Conflict with message
- UserNotFoundException → 404 Not Found with message
- MethodArgumentNotValidException → 400 Bad Request with list of validation error details

---

## application.yml Configuration
Configure the following using environment variables:
- server port: 8081
- spring datasource url from DB_URL
- spring datasource username from DB_USERNAME
- spring datasource password from DB_PASSWORD
- driver: org.postgresql.Driver
- jpa hibernate ddl-auto: update
- jpa show-sql: true
- jpa PostgreSQL dialect
- jpa format_sql: true

---

## Environment Variables Required
- DB_URL → jdbc:postgresql://localhost:5432/db-registration
- DB_USERNAME → videostreamregistrationuser
- DB_PASSWORD → to be defined by the developer

---

## Important Rules
1. Never hardcode sensitive values, always use environment variables
2. The Controller must always inject the UserService interface, never UserServiceImpl
3. DTOs must never reach the Repository layer
4. Entities must never be returned directly from the Controller
5. All mapping between Entity and DTO must go through UserMapper
6. Use Lombok annotations on all classes (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
7. Use @Valid on RegisterRequest parameter in the Controller
8. The register method in UserServiceImpl must be annotated with @Transactional

---

## Task: Password Hashing
Modifie le service d'enregistrement utilisateur pour hasher le mot de passe avant de le sauvegarder en base de données.

1. Ajouter le Bean `PasswordEncoder` dans `SecurityConfig.java` :
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

2. Modifier `UserServiceImpl.java` :
- Injecter `PasswordEncoder` via `@RequiredArgsConstructor`
- Dans la méthode `register()`, encoder le mot de passe avant de builder l'entité `User` :
```java
.password(passwordEncoder.encode(request.getPassword()))
```

## Contraintes
- Ne pas modifier les autres méthodes
- Garder le style du code existant (Lombok, Builder pattern)
- Ne pas retourner le mot de passe hashé dans `UserResponse`
- Algorithme : BCrypt

--

## Add keycloak
The project is a Spring Boot 4.0.6 REST API for a video streaming platform.
Architecture is strict MVC with interface/implementation pattern.
Security is handled by Keycloak 26.0.0 as Identity Provider.
The DB stores only business data: firstName, lastName, age, keycloakId.
Keycloak stores only: email, password (hashed), TOTP secret.
The password must NEVER be stored in the local database.

## What is already done
- Full MVC architecture: UserController, UserService interface, UserServiceImpl, UserRepository, UserMapper, User entity
- SecurityConfig with OAuth2 Resource Server and BCrypt PasswordEncoder bean
- User entity has: id (UUID), firstName, lastName, age, email, createdAt
- RegisterRequest has: firstName, lastName, age, email, password
- UserResponse has: id, firstName, lastName, age, email, createdAt
- UserServiceImpl saves user in DB with BCrypt encoded password

## What to do now

1. Add this dependency to pom.xml:
   keycloak-admin-client version 26.0.0

2. Remove password field from User entity completely.
   Password must never be stored in the database.

3. Update UserMapper.java:
   Remove password mapping from toEntity method.

4. Create KeycloakAdminConfig.java in config package:
   - Spring @Configuration class
   - Creates a Keycloak Admin Client bean using CLIENT_CREDENTIALS grant type
   - Reads from environment variables:
     KEYCLOAK_URL, KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET

5. Create KeycloakService.java in service package:
   - Annotated with @Service
   - Injects Keycloak Admin Client bean and realm name from environment variable
   - Method: createKeycloakUser(String email, String password) returns String keycloakId
   - Creates user in Keycloak: username=email, email=email, enabled=true
   - Sets password as CredentialRepresentation, temporary=false
   - Adds CONFIGURE_TOTP as required action
   - Returns keycloakId using CreatedResponseUtil.getCreatedId(response)
   - Throws RuntimeException with message if response status is not 201

6. Update UserServiceImpl register method:
   - Remove BCrypt password encoding (password goes to Keycloak only)
   - Remove PasswordEncoder injection
   - Keep: email duplicate check, map RegisterRequest to User, save to DB
   - After saving, call keycloakService.createKeycloakUser(email, password)
   - Update saved user with returned keycloakId
   - Save user again
   - Method stays @Transactional for automatic rollback if Keycloak fails
   - Inject KeycloakService via @RequiredArgsConstructor

7. Update UserResponse.java:
   Add keycloakId field (String)

8. Update UserMapper toResponse method:
   Include keycloakId in mapping

9. Update application.yml:
   Add under spring.security.oauth2.resourceserver.jwt:
     issuer-uri: ${KEYCLOAK_ISSUER_URI}
     jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}
   Add keycloak section:
     url: ${KEYCLOAK_URL}
     realm: ${KEYCLOAK_REALM}
     client-id: ${KEYCLOAK_CLIENT_ID}
     client-secret: ${KEYCLOAK_CLIENT_SECRET}
