package com.example.labdata_main.api.response;

import com.example.labdata_main.model.Device;

public class DeviceResponse {
    private String id;
    private String type;
    private String manufacturer;
    private String model;
    private String purchaseYear;
    private String companyId;

    // Default constructor
    public DeviceResponse() {
    }

    // Convert to Device model
    public Device toDevice() {
        Device device = new Device();
        device.setId(id);
        device.setType(type);
        device.setManufacturer(manufacturer);
        device.setModel(model);
        device.setPurchaseYear(purchaseYear);
        device.setCompanyId(companyId);
        return device;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getPurchaseYear() {
        return purchaseYear;
    }

    public void setPurchaseYear(String purchaseYear) {
        this.purchaseYear = purchaseYear;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
