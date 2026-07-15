package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.exception.DuplicateEmailException;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered.");
        }

        return userRepository.save(user);
    }
}