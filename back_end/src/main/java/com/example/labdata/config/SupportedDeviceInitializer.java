package com.example.labdata.config;

import com.example.labdata.model.SupportedDevice;
import com.example.labdata.repository.SupportedDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * u521du59cbu5316u652fu6301u8bbeu5907u6570u636eu7684u914du7f6eu7c7b
 * u5c06u539fu6765u786cu7f16u7801u5728u524du7aefu7684u8bbeu5907u4fe1u606fu8fc1u79fbu5230u540eu7aefu6570u636eu5e93
 */
@Configuration
public class SupportedDeviceInitializer {

    // u8bbeu5907u7c7bu578bu5e38u91cf
    private static final String TYPE_MIXING = "MIXING";      // u62ccu5408u8bbeu5907
    private static final String TYPE_FORMING = "FORMING";    // u5236u4ef6u8bbeu5907
    private static final String TYPE_TESTING = "TESTING";    // u5b9eu9a8cu8bbeu5907
    private static final String TYPE_SIEVING = "SIEVING";    // u7b5bu5206u8bbeu5907

    // u5382u5546u4fe1u606f
    private static final String MANUFACTURER_INFRATEST = "infratest";
    private static final String MANUFACTURER_CONTROLS = "Controls";
    private static final String MANUFACTURER_JILISEN = "u6d59u6c5fu5409u529bu68ee";
    private static final String MANUFACTURER_TANKUANG = "u6d59u6c5fu63a2u77ff";

    @Autowired
    private SupportedDeviceRepository supportedDeviceRepository;

    /**
     * u53eau5728u975eu6d4bu8bd5u73afu5883u4e0bu521du59cbu5316u6570u636e
     */
    @Bean
    @Profile("!test")
    public CommandLineRunner initSupportedDevices() {
        return args -> {
            // u5982u679cu6570u636eu5e93u4e2du5df2u6709u8bbeu5907u6570u636euff0cu5219u4e0du518du521du59cbu5316
            if (supportedDeviceRepository.count() > 0) {
                return;
            }

            System.out.println("Initializing supported device data...");

            // u62ccu5408u8bbeu5907
            saveSupportedDevices(TYPE_MIXING, MANUFACTURER_INFRATEST, 
                    Arrays.asList(new DeviceModelInfo("20-0160-60", "u6c99u7816u5f0fu6c99u6d53u62ccu5408u673a")));

            saveSupportedDevices(TYPE_MIXING, MANUFACTURER_CONTROLS, 
                    Arrays.asList(new DeviceModelInfo("77-PV0077/C", "u81eau52a8u6c99u6d53u62ccu5408u673a")));

            // u5236u4ef6u8bbeu5907
            saveSupportedDevices(TYPE_FORMING, MANUFACTURER_INFRATEST, Arrays.asList(
                    new DeviceModelInfo("20-1500", "u9a6cu6b47u5c14u51fbu5b9eu4eea"),
                    new DeviceModelInfo("60-0220", "u5236u6837u5207u5272u673a")));

            saveSupportedDevices(TYPE_FORMING, MANUFACTURER_CONTROLS, Arrays.asList(
                    new DeviceModelInfo("77-PV41A02", "u81eau52a8u9a6cu6b47u5c14u51fbu5b9eu4eea"),
                    new DeviceModelInfo("77-PV75202", "u6c99u6d53u5236u6837u5207u5272u673a")));

            // u5b9eu9a8cu8bbeu5907
            saveSupportedDevices(TYPE_TESTING, MANUFACTURER_INFRATEST, 
                    Arrays.asList(new DeviceModelInfo("20-1672", "u9a6cu6b47u5c14u7a33u5b9au5ea6u4eea")));

            saveSupportedDevices(TYPE_TESTING, MANUFACTURER_CONTROLS, 
                    Arrays.asList(new DeviceModelInfo("76-B3002", "u6570u5b57u5316u9a6cu6b47u5c14u7a33u5b9au5ea6u4eea")));

            // u7b5bu5206u8bbeu5907
            saveSupportedDevices(TYPE_SIEVING, MANUFACTURER_JILISEN, 
                    Arrays.asList(new DeviceModelInfo("ZBSX-92A", "u96c6u6599u7b5bu5206u4eea")));

            saveSupportedDevices(TYPE_SIEVING, MANUFACTURER_TANKUANG, 
                    Arrays.asList(new DeviceModelInfo("8411", "u9ad8u7cbeu5ea6u96c6u6599u7b5bu5206u4eea")));

            System.out.println("Supported device data initialization completed.");
        };
    }

    /**
     * u4fddu5b58u652fu6301u8bbeu5907u6570u636e
     */
    private void saveSupportedDevices(String type, String manufacturer, List<DeviceModelInfo> models) {
        for (DeviceModelInfo modelInfo : models) {
            SupportedDevice device = new SupportedDevice(type, manufacturer, modelInfo.getModel(), modelInfo.getDescription());
            supportedDeviceRepository.save(device);
        }
    }

    /**
     * u7528u4e8eu5b58u50a8u8bbeu5907u578bu53f7u548cu63cfu8ff0u7684u5185u90e8u7c7b
     */
    private static class DeviceModelInfo {
        private final String model;
        private final String description;

        public DeviceModelInfo(String model, String description) {
            this.model = model;
            this.description = description;
        }

        public String getModel() {
            return model;
        }

        public String getDescription() {
            return description;
        }
    }
}
