package com.Perfume_e_commerce.models.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String passwordHash;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be USER or ADMIN")
    private String role;

    @Builder.Default
    private boolean isVerified = false;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String imageUrl;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @Builder.Default
    private List<String> viewedProductIds = new ArrayList<>();

    @Builder.Default
    private List<WishlistItem> wishlist = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistItem {
        private String productId;
        private String size;
    }
}
