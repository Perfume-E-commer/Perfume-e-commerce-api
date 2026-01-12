package com.Perfume_e_commerce.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String avatarUrl;

    private String street;
    private String city;
    private String zipCode;

    public UserProfileResponse(String id, String firstName, String lastName, String email, String role, String avatarUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.street = "";
        this.city = "";
        this.zipCode = "";
    }
}
