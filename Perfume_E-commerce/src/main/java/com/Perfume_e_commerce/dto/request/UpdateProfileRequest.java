package com.Perfume_e_commerce.dto.request;

import com.Perfume_e_commerce.models.CreditCard;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    // Address fields (to update the main address)
    private String street;
    private String city;
    private String zipCode;

    private String imageUrl;
    private List<CreditCard> creditCards;
}
