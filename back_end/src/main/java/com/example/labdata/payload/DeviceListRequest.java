package com.example.labdata.payload;

import lombok.Data;

import java.util.List;

@Data
public class DeviceListRequest {
    private List<DeviceRequest> devices;
}
