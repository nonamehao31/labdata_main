package com.example.labdata.controller;

import com.example.labdata.model.Device;
import com.example.labdata.model.User;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.payload.DeviceRequest;
import com.example.labdata.payload.DeviceResponse;
import com.example.labdata.repository.DeviceRepository;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> saveDevice(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody DeviceRequest deviceRequest) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Device device = new Device();
        device.setId(deviceRequest.getId());
        device.setType(deviceRequest.getType());
        device.setManufacturer(deviceRequest.getManufacturer());
        device.setModel(deviceRequest.getModel());
        device.setPurchaseYear(deviceRequest.getPurchaseYear());
        device.setCompanyId(deviceRequest.getCompanyId());
        device.setUser(user);

        Device savedDevice = deviceRepository.save(device);
        return ResponseEntity.ok(new ApiResponse<>(true, "Device saved successfully", DeviceResponse.fromDevice(savedDevice)));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> saveDevices(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody List<DeviceRequest> deviceRequests) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Device> devices = deviceRequests.stream().map(req -> {
            Device device = new Device();
            device.setId(req.getId());
            device.setType(req.getType());
            device.setManufacturer(req.getManufacturer());
            device.setModel(req.getModel());
            device.setPurchaseYear(req.getPurchaseYear());
            device.setCompanyId(req.getCompanyId());
            device.setUser(user);
            return device;
        }).collect(Collectors.toList());

        List<Device> savedDevices = deviceRepository.saveAll(devices);
        List<DeviceResponse> responses = savedDevices.stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Devices saved successfully", responses));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getDevicesByCompany(@PathVariable String companyId) {
        List<Device> devices = deviceRepository.findByCompanyId(companyId);
        List<DeviceResponse> responses = devices.stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Devices retrieved successfully", responses));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserDevices(@CurrentUser UserPrincipal currentUser) {
        List<Device> devices = deviceRepository.findByUserId(currentUser.getId());
        List<DeviceResponse> responses = devices.stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "User devices retrieved successfully", responses));
    }

    /**
     * u68c0u67e5u516cu53f8u662fu5426u5df2u5b8cu6210u8bbeu5907u521du59cbu5316
     * @param companyId u516cu53f8ID
     * @return u5982u679cu5df2u521du59cbu5316u8fd4u56detrueuff0cu5426u5219u8fd4u56defalse
     */
    @GetMapping("/company/{companyId}/initialized")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> isCompanyEquipmentInitialized(@PathVariable String companyId) {
        // u67e5u8be2u516cu53f8u7684u8bbeu5907u6570u91cf
        List<Device> devices = deviceRepository.findByCompanyId(companyId);
        boolean isInitialized = !devices.isEmpty();
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                isInitialized ? "Company has initialized equipment" : "Company has not initialized equipment", 
                isInitialized));
    }
}
