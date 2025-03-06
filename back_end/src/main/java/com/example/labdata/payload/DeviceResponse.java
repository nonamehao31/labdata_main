package com.example.labdata.payload;

import com.example.labdata.model.Device;
import lombok.Data;

@Data
public class DeviceResponse {
    private String id;
    private String type;
    private String manufacturer;
    private String model;
    private String purchaseYear;
    private String companyId;
    
    public static DeviceResponse fromDevice(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setType(device.getType());
        response.setManufacturer(device.getManufacturer());
        response.setModel(device.getModel());
        response.setPurchaseYear(device.getPurchaseYear());
        response.setCompanyId(device.getCompanyId());
        return response;
    }
}
