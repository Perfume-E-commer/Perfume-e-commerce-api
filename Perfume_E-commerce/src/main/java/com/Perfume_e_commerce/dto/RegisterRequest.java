package com.Perfume_e_commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.scheduling.support.SimpleTriggerContext;

@Data
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;
}
