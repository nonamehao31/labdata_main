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
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("companyId")
    private String companyId;
    
    @SerializedName("allowAddMixture")
    private boolean allowAddMixture;
    
    @SerializedName("allowAddAsphalt")
    private boolean allowAddAsphalt;
    
    @SerializedName("allowAddMixratio")
    private boolean allowAddMixratio;
    
    @SerializedName("admin")
    private boolean admin;
    
    // 默认构造函数
    public LoginResponse() {
    }
    
    // 带参数的构造函数
    public LoginResponse(String accessToken, String tokenType, long userId, String username, String name, String companyId,
                         boolean allowAddMixture, boolean allowAddAsphalt, boolean allowAddMixratio, boolean admin) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.companyId = companyId;
        this.allowAddMixture = allowAddMixture;
        this.allowAddAsphalt = allowAddAsphalt;
        this.allowAddMixratio = allowAddMixratio;
        this.admin = admin;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    
    public boolean isAllowAddMixture() {
        return allowAddMixture;
    }
    
    public void setAllowAddMixture(boolean allowAddMixture) {
        this.allowAddMixture = allowAddMixture;
    }
    
    public boolean isAllowAddAsphalt() {
        return allowAddAsphalt;
    }
    
    public void setAllowAddAsphalt(boolean allowAddAsphalt) {
        this.allowAddAsphalt = allowAddAsphalt;
    }
    
    public boolean isAllowAddMixratio() {
        return allowAddMixratio;
    }
    
    public void setAllowAddMixratio(boolean allowAddMixratio) {
        this.allowAddMixratio = allowAddMixratio;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
