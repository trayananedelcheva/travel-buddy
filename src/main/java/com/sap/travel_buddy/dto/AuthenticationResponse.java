package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO за authentication response (JWT токен)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    
    private String token;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
