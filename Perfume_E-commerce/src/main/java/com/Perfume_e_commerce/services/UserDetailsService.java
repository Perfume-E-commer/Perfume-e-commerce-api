package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.CreditCard;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

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
}
