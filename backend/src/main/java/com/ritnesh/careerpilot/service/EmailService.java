package com.ritnesh.careerpilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Sends OTP emails via Brevo's HTTP API instead of raw SMTP.
 *
 * Why: hosting providers (Render's free tier included) commonly block
 * outbound traffic on SMTP ports (25/465/587) to prevent spam abuse.
 * An HTTPS-based email API sends over port 443, which is never blocked,
 * so this works identically whether running locally or deployed.
 */
@Service
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.mail.sender}")
    private String senderEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "CareerPilot", "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Your CareerPilot verification code",
                "textContent",
                "Your CareerPilot verification code is: " + otpCode +
                        "\n\nThis code expires in 10 minutes.\n\n" +
                        "If you didn't request this, you can ignore this email.\n\n" +
                        "Best Regards.\n\n" +
                        "Ritnesh Srivastav.\n\n"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Any failure here (bad key, network issue) throws, which UserService
        // already handles: the account still exists unverified, and
        // resend-otp lets the user recover without losing their registration.
        restTemplate.postForEntity(BREVO_API_URL, request, String.class);
    }
}
