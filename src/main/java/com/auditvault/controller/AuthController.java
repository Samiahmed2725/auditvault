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

import com.auditvault.dto.LoginRequest;
import com.auditvault.dto.LoginResponse;
import com.auditvault.security.CustomUserDetails;
import com.auditvault.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.auditvault.service.AuditService auditService; // Inject AuditService

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          com.auditvault.service.AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        System.out.println("🔥 LOGIN ENDPOINT HIT");
        System.out.println("📧 Email: " + request.getEmail());
        System.out.println("🔑 Password: " + request.getPassword());

        CustomUserDetails userDetails = (CustomUserDetails) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        ).getPrincipal();

        String token = jwtUtil.generateToken(request.getEmail());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        
        // Log Login Action
        // Need a User object. We can construct a minimal one or fetch. 
        // Better to use the info we have. AuditService takes User.
        // Let's create a temporary User object or fetch it if needed.
        // Actually, let's fetch strictly or just pass fields. 
        // AuditService.logAction(User user...
        // Let's modify AuditService to be flexible OR construct a User obj wrapper.
        // Constructing a User object from details is cleaner than fetching again.
        com.auditvault.model.User logUser = new com.auditvault.model.User();
        logUser.setId(userDetails.getId());
        logUser.setEmail(userDetails.getEmail());
        logUser.setRole(com.auditvault.model.User.Role.valueOf(role.replace("ROLE_", "")));

        auditService.logAction(logUser, com.auditvault.model.AuditLog.Action.LOGIN, "IP: Request"); 

        return new LoginResponse(
                userDetails.getId(),
                userDetails.getName(),
                request.getEmail(),
                role,
                token
        );
    }
}

