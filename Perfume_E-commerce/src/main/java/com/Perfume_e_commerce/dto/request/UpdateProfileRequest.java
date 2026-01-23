package com.Perfume_e_commerce.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private String street;
    private String city;
    private String zipCode;

    private String imageUrl;
}
