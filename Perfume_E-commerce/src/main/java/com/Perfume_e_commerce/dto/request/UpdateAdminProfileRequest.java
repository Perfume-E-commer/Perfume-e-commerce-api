package com.Perfume_e_commerce.dto.request;

import lombok.Data;

@Data
public class UpdateAdminProfileRequest {
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
