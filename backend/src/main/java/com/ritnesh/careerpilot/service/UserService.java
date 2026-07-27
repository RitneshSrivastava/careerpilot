package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.LoginRequest;
import com.ritnesh.careerpilot.dto.LoginResponse;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.exception.AccountNotVerifiedException;
import com.ritnesh.careerpilot.exception.DuplicateEmailException;
import com.ritnesh.careerpilot.exception.InvalidCredentialsException;
import com.ritnesh.careerpilot.exception.InvalidOtpException;
import com.ritnesh.careerpilot.repository.UserRepository;
import com.ritnesh.careerpilot.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_VALIDITY_MINUTES = 10;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));

        User savedUser = userRepository.save(user);

        // If email sending fails (bad app password, network issue, etc.), the
        // account still exists but unverified - the user can be sent a fresh
        // OTP via resendOtp() rather than losing the registration entirely.
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        return savedUser;
    }

    public void verifyOtp(String email, String code) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("No account found for this email."));

        if (user.isVerified()) {
            throw new InvalidOtpException("This account is already verified.");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(code)) {
            throw new InvalidOtpException("Invalid verification code.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("This code has expired. Please request a new one.");
        }

        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public void resendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("No account found for this email."));

        if (user.isVerified()) {
            throw new InvalidOtpException("This account is already verified.");
        }

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password.")
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        if (!user.isVerified()) {
            throw new AccountNotVerifiedException(
                    "Please verify your email before logging in. Check your inbox for the code, " +
                            "or request a new one via /api/users/resend-otp.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                "Login Successful",
                user.getFullName(),
                user.getEmail(),
                token
        );
    }

    private String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }
}
