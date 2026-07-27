package com.ritnesh.careerpilot.controller;
import com.ritnesh.careerpilot.dto.RegisterRequest;
import com.ritnesh.careerpilot.dto.RegisterResponse;
import com.ritnesh.careerpilot.dto.ResendOtpRequest;
import com.ritnesh.careerpilot.dto.VerifyOtpRequest;
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
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request) {

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.registerUser(user);

        // Never return the raw User entity here - it carries the BCrypt password
        // hash, and returning it would leak that hash in the API response.
        RegisterResponse response = new RegisterResponse(
                "Registered. Check your email for a verification code.",
                savedUser.getFullName(),
                savedUser.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        userService.verifyOtp(request.getEmail(), request.getCode());

        return ResponseEntity.ok("Account verified successfully. You can now log in.");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@Valid @RequestBody ResendOtpRequest request) {

        userService.resendOtp(request.getEmail());

        return ResponseEntity.ok("A new verification code has been sent.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.loginUser(request));

    }
    @GetMapping("/profile")
    public ResponseEntity<String> getProfile() {
        return ResponseEntity.ok("Welcome to CareerPilot! JWT Authentication Successful.");
    }
}
