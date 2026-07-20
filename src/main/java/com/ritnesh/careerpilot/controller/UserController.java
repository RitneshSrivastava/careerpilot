package com.ritnesh.careerpilot.controller;
import com.ritnesh.careerpilot.dto.RegisterRequest;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ritnesh.careerpilot.dto.LoginRequest;
import com.ritnesh.careerpilot.dto.LoginResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody RegisterRequest request) {

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.loginUser(request));

    }
}