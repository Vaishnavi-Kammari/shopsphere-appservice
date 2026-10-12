package com.shopsphere.service;

import com.shopsphere.dto.Dtos.AuthResponse;
import com.shopsphere.dto.Dtos.LoginRequest;
import com.shopsphere.dto.Dtos.RegisterRequest;
import com.shopsphere.dto.Dtos.UserDto;
import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.Role;
import com.shopsphere.repository.UserRepository;
import com.shopsphere.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered");
        }
        AppUser user = users.save(new AppUser(request.name().trim(), email,
                encoder.encode(request.password()), Role.CUSTOMER));
        return new AuthResponse(tokens.create(user.getEmail()), toDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = users.findByEmail(email)
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(tokens.create(user.getEmail()), toDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto me(String email) {
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toDto(user);
    }

    private static UserDto toDto(AppUser u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
