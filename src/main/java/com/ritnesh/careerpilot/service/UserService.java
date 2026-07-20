package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.LoginRequest;
import com.ritnesh.careerpilot.dto.LoginResponse;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.exception.DuplicateEmailException;
import com.ritnesh.careerpilot.exception.InvalidCredentialsException;
import com.ritnesh.careerpilot.repository.UserRepository;
import com.ritnesh.careerpilot.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password.")
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                "Login Successful",
                user.getFullName(),
                user.getEmail(),
                token
        );
    }
}