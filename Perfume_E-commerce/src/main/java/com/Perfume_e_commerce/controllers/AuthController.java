package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.AuthResponse;
import com.Perfume_e_commerce.dto.LoginRequest;
import com.Perfume_e_commerce.dto.RegisterRequest;
import com.Perfume_e_commerce.models.User;
import com.Perfume_e_commerce.models.VerificationCode;
import com.Perfume_e_commerce.security.JwtUtils;
import com.Perfume_e_commerce.services.EmailService;
import com.Perfume_e_commerce.services.UserDetailsService;
import com.Perfume_e_commerce.services.VerificationCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.emailExists(registerRequest.getEmail())) {
            User existingUser = userService.findByEmail(registerRequest.getEmail()).get();
            if (existingUser.isVerified()) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            } else {
                return ResponseEntity.badRequest().body("Error: Account pending verification. Please verify or use a different email.");
            }
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole("USER"); 
        user.setVerified(false);

        userService.saveUser(user);
        VerificationCode vc = verificationCodeService.createVerificationCode(user.getEmail());

        emailService.sendVerificationEmail(user.getEmail(), vc.getCode());

        return ResponseEntity.ok("Verifying code has been sent to your email! Please check and verify your register!!");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
        Optional<VerificationCode> vcOpt = verificationCodeService.findByEmail(email);

        if (vcOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: No verification code found for this email");
        }

        VerificationCode vc = vcOpt.get();

        if (!vc.getCode().equals(code)) {
            return ResponseEntity.badRequest().body("Error: Invalid verification code");
        }

        if (vc.isExpired()) {
            verificationCodeService.deleteByEmail(email);
            return ResponseEntity.badRequest().body("Error: Verification code has expired");
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: User not found");
        }

        User user = userOpt.get();
        user.setVerified(true);
        userService.saveUser(user);

        // Clean up used code using service
        verificationCodeService.deleteByEmail(email);

        return ResponseEntity.ok("User verified successfully!");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        if (!userService.emailExists(email)) {
            return ResponseEntity.badRequest().body("Error: Email not found");
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().isVerified()) {
            return ResponseEntity.badRequest().body("Error: User already verified");
        }

        VerificationCode vc = verificationCodeService.createVerificationCode(email);
        emailService.sendVerificationEmail(email, vc.getCode());

        return ResponseEntity.ok("Verification code sent successfully!");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e){
            return ResponseEntity.status(401).body("Error: Invalid credentials");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority().replace("ROLE_", "")) // Clean up "ROLE_"
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AuthResponse(jwtToken, userDetails.getUsername(), roles.get(0)));
    }
}
