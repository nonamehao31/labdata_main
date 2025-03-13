package com.example.labdata.controller;

import com.example.labdata.model.SupportedDevice;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.repository.SupportedDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supported-devices")
public class SupportedDeviceController {

    @Autowired
    private SupportedDeviceRepository supportedDeviceRepository;

    /**
     * u83b7u53d6u6240u6709u652fu6301u7684u8bbeu5907
     */
    @GetMapping
    public ResponseEntity<?> getAllSupportedDevices() {
        List<SupportedDevice> devices = supportedDeviceRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "All supported devices retrieved successfully", devices));
    }

    /**
     * u83b7u53d6u6307u5b9au7c7bu578bu7684u8bbeu5907
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getSupportedDevicesByType(@PathVariable String type) {
        List<SupportedDevice> devices = supportedDeviceRepository.findByType(type);
        return ResponseEntity.ok(new ApiResponse<>(true, "Devices by type retrieved successfully", devices));
    }

    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu7684u6240u6709u5382u5546
     */
    @GetMapping("/type/{type}/manufacturers")
    public ResponseEntity<?> getManufacturersByType(@PathVariable String type) {
        List<String> manufacturers = supportedDeviceRepository.findDistinctManufacturerByType(type);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturers by type retrieved successfully", manufacturers));
    }

    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu7684u6240u6709u5382u5546
     */
    @GetMapping("/manufacturers/{type}")
    public ResponseEntity<?> getManufacturersByTypeOld(@PathVariable String type) {
        return getManufacturersByType(type);
    }

    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu548cu5382u5546u7684u578bu53f7
     */
    @GetMapping("/type/{type}/manufacturer/{manufacturer}/models")
    public ResponseEntity<?> getModelsByTypeAndManufacturerNew(
            @PathVariable String type,
            @PathVariable String manufacturer) {
        List<SupportedDevice> devices = supportedDeviceRepository.findByTypeAndManufacturer(type, manufacturer);
        List<String> models = devices.stream()
                .map(SupportedDevice::getModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Models retrieved successfully", models));
    }

    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu548cu5382u5546u7684u578bu53f7
     */
    @GetMapping("/models/{type}/{manufacturer}")
    public ResponseEntity<?> getModelsByTypeAndManufacturer(
            @PathVariable String type,
            @PathVariable String manufacturer) {
        List<SupportedDevice> devices = supportedDeviceRepository.findByTypeAndManufacturer(type, manufacturer);
        List<String> models = devices.stream()
                .map(SupportedDevice::getModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Models retrieved successfully", models));
    }

    /**
     * u6dfbu52a0u65b0u7684u652fu6301u8bbeu5907
     * u9650u7ba1u7406u5458u4f7fu7528
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSupportedDevice(@Valid @RequestBody SupportedDevice supportedDevice) {
        SupportedDevice savedDevice = supportedDeviceRepository.save(supportedDevice);
        return ResponseEntity.ok(new ApiResponse<>(true, "Supported device added successfully", savedDevice));
    }

    /**
     * u6279u91cfu6dfbu52a0u652fu6301u8bbeu5907
     * u9650u7ba1u7406u5458u4f7fu7528
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSupportedDevicesBatch(@Valid @RequestBody List<SupportedDevice> supportedDevices) {
        List<SupportedDevice> savedDevices = supportedDeviceRepository.saveAll(supportedDevices);
        return ResponseEntity.ok(new ApiResponse<>(true, "Supported devices added successfully", savedDevices));
    }

    /**
     * u66f4u65b0u652fu6301u8bbeu5907
     * u9650u7ba1u7406u5458u4f7fu7528
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSupportedDevice(
            @PathVariable Long id,
            @Valid @RequestBody SupportedDevice supportedDeviceRequest) {
        return supportedDeviceRepository.findById(id).map(existingDevice -> {
            existingDevice.setType(supportedDeviceRequest.getType());
            existingDevice.setManufacturer(supportedDeviceRequest.getManufacturer());
            existingDevice.setModel(supportedDeviceRequest.getModel());
            existingDevice.setDescription(supportedDeviceRequest.getDescription());
            SupportedDevice updatedDevice = supportedDeviceRepository.save(existingDevice);
            return ResponseEntity.ok(new ApiResponse<>(true, "Supported device updated successfully", updatedDevice));
        }).orElseThrow(() -> new RuntimeException("Supported device not found with id " + id));
    }

    /**
     * u5220u9664u652fu6301u8bbeu5907
     * u9650u7ba1u7406u5458u4f7fu7528
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSupportedDevice(@PathVariable Long id) {
        return supportedDeviceRepository.findById(id).map(device -> {
            supportedDeviceRepository.delete(device);
            return ResponseEntity.ok(new ApiResponse<>(true, "Supported device deleted successfully", null));
        }).orElseThrow(() -> new RuntimeException("Supported device not found with id " + id));
    }
}
