package com.Perfume_e_commerce.dto.request;

import lombok.Data;

@Data
public class UpdateAdminProfileRequest {
    private String firstName;
    private String lastName;
    private String avatarUrl;
}
