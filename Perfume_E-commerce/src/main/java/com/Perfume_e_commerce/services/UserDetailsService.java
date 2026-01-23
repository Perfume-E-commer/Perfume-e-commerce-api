package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.ChangePasswordRequest;
import com.Perfume_e_commerce.dto.request.UpdateProfileRequest;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.CreditCard;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.Perfume_e_commerce.services.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User updateVerificationStatus(String email, boolean verified) {
        User user = findByEmailOrThrow(email);
        user.setVerified(verified);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        return UserDetailsImpl.build(user);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null)
            user.setDateOfBirth(request.getDateOfBirth());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            user.setImageUrl(request.getImageUrl());
        }
        return userRepository.save(user);
    }

    public User addAddress(String email, Address address) {
        User user = findByEmailOrThrow(email);

        if (user.getAddresses().isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            user.getAddresses().forEach(a -> a.setDefault(false));
        }

        user.getAddresses().add(address);
        return userRepository.save(user);
    }

    public User addCreditCard(String email, CreditCard card) {
        User user = findByEmailOrThrow(email);

        card.setId(UUID.randomUUID().toString()); // Generate ID
        user.getCreditCards().add(card);

        return userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmailOrThrow(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public String uploadAvatar(String email, MultipartFile file) {
        User user = findByEmailOrThrow(email);

        // Use FileStorageService to store files under the unified uploads directory
        String relativePath = fileStorageService.storeFile(file); // returns path like "/api/uploads/<filename>"

        // Build absolute URL based on current request context
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(relativePath)
                .toUriString();

        user.setImageUrl(fileUrl);
        userRepository.save(user);

        return fileUrl;
    }

    public User deleteAddress(String email, String addressId) {
        User user = findByEmailOrThrow(email);

        user.getAddresses().removeIf(addr -> addr.getId() != null && addr.getId().equals(addressId));

        return userRepository.save(user);
    }

    public User deleteCreditCard(String email, String cardId) {
        User user = findByEmailOrThrow(email);

        user.getCreditCards().removeIf(card -> card.getId() != null && card.getId().equals(cardId));

        return userRepository.save(user);
    }
}
