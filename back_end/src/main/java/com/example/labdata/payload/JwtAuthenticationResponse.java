package com.example.labdata.payload;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String organization;
    private String companyId;

    public JwtAuthenticationResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
    }
    
    public JwtAuthenticationResponse(String accessToken, Long userId, String username, String organization, String companyId) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.organization = organization;
        this.companyId = companyId;
    }
}
