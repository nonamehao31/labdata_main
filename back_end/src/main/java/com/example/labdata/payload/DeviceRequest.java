package com.example.labdata.payload;

import lombok.Data;

@Data
public class DeviceRequest {
    private String id;
    private String type;
    private String manufacturer;
    private String model;
    private String purchaseYear;
    private String companyId;
}
