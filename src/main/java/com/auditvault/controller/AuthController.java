//package com.auditvault.controller;
//
//import com.auditvault.dto.LoginResponse;
//import com.auditvault.security.CustomUserDetails;
//import com.auditvault.security.jwt.JwtUtil;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//
//    public AuthController(AuthenticationManager authenticationManager,
//                          JwtUtil jwtUtil) {
//        this.authenticationManager = authenticationManager;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @PostMapping("/login")
//    public LoginResponse login(
//            @RequestParam String email,
//            @RequestParam String password
//    ) {
//        Authentication auth = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(email, password)
//        );
//
//        CustomUserDetails userDetails =
//                (CustomUserDetails) auth.getPrincipal();
//
//
//        // ✅ JWT GENERATED HERE
//        String token = jwtUtil.generateToken(userDetails.getEmail());
//
//        return new LoginResponse(
//                userDetails.getId(),
//                userDetails.getEmail(),
//                userDetails.getAuthorities().iterator().next().getAuthority(),
//                token
//        );
//
//    }
//


package com.auditvault.controller;

import com.auditvault.dto.ChangePasswordRequest;
import com.auditvault.dto.ForgotPasswordRequest;
import com.auditvault.dto.LoginRequest;
import com.auditvault.dto.LoginResponse;
import com.auditvault.dto.ResetPasswordRequest;
import com.auditvault.security.CustomUserDetails;
import com.auditvault.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.auditvault.service.AuditService auditService; // Inject AuditService
    private final com.auditvault.repository.UserRepository userRepository;
    private final com.auditvault.service.PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          com.auditvault.service.AuditService auditService,
                          com.auditvault.repository.UserRepository userRepository,
                          com.auditvault.service.PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "userId is required");
        }

        try {
            String cleanUserId = request.getUserId().trim().toLowerCase();
            CustomUserDetails userDetails = (CustomUserDetails) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            cleanUserId,
                            request.getPassword()
                    )
            ).getPrincipal();

            String token = jwtUtil.generateToken(userDetails.getUserId());
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            // Log Login Action
            com.auditvault.model.User logUser = userRepository.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            auditService.logAction(logUser, com.auditvault.model.AuditLog.Action.LOGIN, "Login successful");

            return new LoginResponse(
                    userDetails.getId(),
                    userDetails.getUserId(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    role,
                    token
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Temporarily disabled
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_IMPLEMENTED, "Feature not enabled");
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        // Temporarily disabled
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_IMPLEMENTED, "Feature not enabled");
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "currentPassword is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "newPassword is required");
        }

        com.auditvault.model.User user = getAuthenticatedUser();
        passwordResetService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        auditService.logAction(user, com.auditvault.model.AuditLog.Action.CHANGE_PASSWORD, "Password changed");
    }

    private com.auditvault.model.User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

