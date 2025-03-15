package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 登录响应数据类
 */
public class LoginResponse {
    
    @SerializedName("accessToken")
    private String accessToken;
    
    @SerializedName("tokenType")
    private String tokenType;
    
    @SerializedName("userId")
    private long userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("companyId")
    private String companyId;
    
    // 默认构造函数
    public LoginResponse() {
    }
    
    // 带参数的构造函数
    public LoginResponse(String accessToken, String tokenType, long userId, String username, String companyId) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.username = username;
        this.companyId = companyId;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
