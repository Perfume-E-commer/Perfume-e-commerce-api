package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.ChangePasswordRequest;
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
                () -> new UsernameNotFoundException("User not found with email: " + email)
        );

        return UserDetailsImpl.build(user);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public User updateProfile(String email, User updatedData) {
        User user = findByEmailOrThrow(email);

        if (updatedData.getFirstName() != null) user.setFirstName(updatedData.getFirstName());
        if (updatedData.getLastName() != null) user.setLastName(updatedData.getLastName());
        if (updatedData.getPhoneNumber() != null) user.setPhoneNumber(updatedData.getPhoneNumber());
        if (updatedData.getDateOfBirth() != null) user.setDateOfBirth(updatedData.getDateOfBirth());
        // imageUrl, gender, etc. if you have them

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
            throw new RuntimeException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public String uploadAvatar(String email, MultipartFile file) {
        User user = findByEmailOrThrow(email);

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://localhost:8080/api/uploads/avatars/" + filename;

            user.setImageUrl(fileUrl);
            userRepository.save(user);

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public User deleteAddress(String email, String addressId) {
        User user = findByEmailOrThrow(email);

        user.getAddresses().removeIf(addr ->
                addr.getId() != null && addr.getId().equals(addressId)
        );

        return userRepository.save(user);
    }

    public User deleteCreditCard(String email, String cardId) {
        User user = findByEmailOrThrow(email);

        user.getCreditCards().removeIf(card ->
                card.getId() != null && card.getId().equals(cardId)
        );

        return userRepository.save(user);
    }
}
