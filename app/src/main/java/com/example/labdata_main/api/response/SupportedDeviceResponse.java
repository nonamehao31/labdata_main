package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * u652fu6301u8bbeu5907u54cdu5e94u6a21u578b
 */
public class SupportedDeviceResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("manufacturer")
    private String manufacturer;
    
    @SerializedName("model")
    private String model;
    
    @SerializedName("description")
    private String description;
    
    // u7f3au7701u6784u9020u5668
    public SupportedDeviceResponse() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
