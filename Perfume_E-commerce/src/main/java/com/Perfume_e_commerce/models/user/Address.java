package com.Perfume_e_commerce.models.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @NotBlank
    private String houseNumber;
    @NotBlank
    private String street;
    @NotBlank
    private String village;
    @NotBlank
    private String community;
    @NotBlank
    private String district;
    @NotBlank
    private String city;

    @NotBlank
    private String fullName;

    @NotBlank
    private String zipCode;
    private String phoneNumber;

    @Builder.Default
    private String type = "HOME";

    private boolean isDefault;

    public String getFullAddress() {
        return String.format("#%s, St. %s, %s, %s, %s, %s",
                houseNumber, street, village, community, district, city);
    }
}
