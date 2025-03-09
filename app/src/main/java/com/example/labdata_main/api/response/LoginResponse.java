package com.example.labdata_main.api.response;

/**
 * 登录响应
 */
public class LoginResponse {
    private String accessToken;
    private Long userId;
    private String username;
    private String tokenType = "Bearer";
    private String companyId;  // 公司ID
    private String companyName;  // 公司名称
    
    public LoginResponse() {
    }
    
    public LoginResponse(String accessToken, Long userId, String username, String companyId, String companyName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.companyId = companyId;
        this.companyName = companyName;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
