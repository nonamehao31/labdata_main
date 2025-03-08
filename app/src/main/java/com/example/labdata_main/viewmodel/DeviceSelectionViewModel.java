package com.example.labdata_main.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.labdata_main.api.response.SupportedDeviceResponse;
import com.example.labdata_main.service.SupportedDeviceService;

import java.util.ArrayList;
import java.util.List;

/**
 * u8bbeu5907u9009u62e9u89c6u56feu6a21u578b
 * u4f7fu7528SupportedDeviceServiceu4eceu540eu7aefu83b7u53d6u8bbeu5907u4fe1u606f
 */
public class DeviceSelectionViewModel extends AndroidViewModel {
    private final SupportedDeviceService deviceService;
    
    private final MutableLiveData<List<String>> deviceTypes = new MutableLiveData<>();
    private final MutableLiveData<List<String>> manufacturers = new MutableLiveData<>();
    private final MutableLiveData<List<String>> models = new MutableLiveData<>();
    
    private final MutableLiveData<String> selectedType = new MutableLiveData<>();
    private final MutableLiveData<String> selectedManufacturer = new MutableLiveData<>();
    private final MutableLiveData<String> selectedModel = new MutableLiveData<>();
    
    public DeviceSelectionViewModel(@NonNull Application application) {
        super(application);
        deviceService = SupportedDeviceService.getInstance();
        
        // u521du59cbu5316u8bbeu5907u7c7bu578bu5217u8868
        deviceTypes.setValue(deviceService.getDeviceTypes());
    }
    
    /**
     * u83b7u53d6u6240u6709u8bbeu5907u7c7bu578b
     */
    public LiveData<List<String>> getDeviceTypes() {
        return deviceTypes;
    }
    
    /**
     * u6839u636eu9009u62e9u7684u7c7bu578bu83b7u53d6u5382u5546u5217u8868
     */
    public LiveData<List<String>> getManufacturers() {
        return manufacturers;
    }
    
    /**
     * u6839u636eu9009u62e9u7684u7c7bu578bu548cu5382u5546u83b7u53d6u578bu53f7u5217u8868
     */
    public LiveData<List<String>> getModels() {
        return models;
    }
    
    /**
     * u83b7u53d6u5f53u524du9009u62e9u7684u7c7bu578b
     */
    public LiveData<String> getSelectedType() {
        return selectedType;
    }
    
    /**
     * u83b7u53d6u5f53u524du9009u62e9u7684u5382u5546
     */
    public LiveData<String> getSelectedManufacturer() {
        return selectedManufacturer;
    }
    
    /**
     * u83b7u53d6u5f53u524du9009u62e9u7684u578bu53f7
     */
    public LiveData<String> getSelectedModel() {
        return selectedModel;
    }
    
    /**
     * u8bbeu7f6eu9009u4e2du7684u7c7bu578bu5e76u52a0u8f7du76f8u5e94u7684u5382u5546
     */
    public void setSelectedType(String type) {
        if (type == null || type.equals(selectedType.getValue())) {
            return;
        }
        
        selectedType.setValue(type);
        selectedManufacturer.setValue(null);
        selectedModel.setValue(null);
        
        // u4eceu540eu7aefu83b7u53d6u5382u5546u5217u8868
        deviceService.getManufacturers(type).observeForever(result -> {
            manufacturers.setValue(result);
            models.setValue(new ArrayList<>());
        });
    }
    
    /**
     * u8bbeu7f6eu9009u4e2du7684u5382u5546u5e76u52a0u8f7du76f8u5e94u7684u578bu53f7
     */
    public void setSelectedManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.equals(selectedManufacturer.getValue())) {
            return;
        }
        
        selectedManufacturer.setValue(manufacturer);
        selectedModel.setValue(null);
        
        // u4eceu540eu7aefu83b7u53d6u578bu53f7u5217u8868
        String type = selectedType.getValue();
        if (type != null) {
            deviceService.getModels(type, manufacturer).observeForever(result -> {
                models.setValue(result);
            });
        }
    }
    
    /**
     * u8bbeu7f6eu9009u4e2du7684u578bu53f7
     */
    public void setSelectedModel(String model) {
        selectedModel.setValue(model);
    }
    
    /**
     * u83b7u53d6u5f53u524du9009u62e9u7684u8bbeu5907u4fe1u606f
     */
    public LiveData<SupportedDeviceResponse> getSelectedDeviceInfo() {
        String type = selectedType.getValue();
        String manufacturer = selectedManufacturer.getValue();
        String model = selectedModel.getValue();
        
        if (type == null || manufacturer == null || model == null) {
            return new MutableLiveData<>(null);
        }
        
        return deviceService.findMatchingDevice(type, manufacturer, model);
    }
    
    /**
     * u6e05u9664u6240u6709u9009u62e9
     */
    public void clearSelections() {
        selectedType.setValue(null);
        selectedManufacturer.setValue(null);
        selectedModel.setValue(null);
        manufacturers.setValue(new ArrayList<>());
        models.setValue(new ArrayList<>());
    }
    
    /**
     * u5237u65b0u6570u636e
     */
    public void refreshData() {
        deviceService.clearCache();
        deviceTypes.setValue(deviceService.getDeviceTypes());
        clearSelections();
    }
}
