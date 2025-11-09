package com.Perfume_e_commerce.security;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private String jwtToken;
    private String username;
    private List<String> roles;

    public LoginResponse(String jwtToken, List<String> roles, String username) {
        this.jwtToken = jwtToken;
        this.roles = roles;
        this.username = username;
    }

}
