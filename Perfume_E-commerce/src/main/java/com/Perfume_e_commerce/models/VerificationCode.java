package com.Perfume_e_commerce.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "verification_codes")
@Getter
@Setter
public class VerificationCode {
    @Id
    private String id;

    private String email;
    private String code;

    // TTL Index: MongoDB deletes this document 300 seconds (5 mins) after 'createdAt'
    @Indexed(name = "otp_expire_index", expireAfter = "PT5M")
    private LocalDateTime createdAt;

    public VerificationCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return createdAt.plusMinutes(5).isBefore(LocalDateTime.now());
    }
}
