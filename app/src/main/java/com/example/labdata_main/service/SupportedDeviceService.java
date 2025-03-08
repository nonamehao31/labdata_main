package com.example.labdata_main.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.SupportedDeviceResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * u652fu6301u8bbeu5907u670du52a1u7c7b
 * u7528u4e8eu7eefu540cu540eu7aefu83b7u53d6u652fu6301u8bbeu5907u4fe1u606f
 */
public class SupportedDeviceService {
    private static final String TAG = "SupportedDeviceService";
    
    private static SupportedDeviceService instance;
    private final ApiService apiService;
    
    // u7f13u5b58
    private final Map<String, List<String>> manufacturerCache = new HashMap<>();  // u5217u51fau7c7bu578buu5bf9u5e94u7684u5de5u5382
    private final Map<String, List<String>> modelCache = new HashMap<>();        // u5217u51fau5de5u5382uu5bf9u5e94u7684u578bu53f7
    private final List<SupportedDeviceResponse> allDevicesCache = new ArrayList<>(); // u6240u6709u8bbeu5907
    
    // u8bbeu5907u7c7bu578bu5e38u91cf
    public static final String TYPE_MIXING = "MIXING";      // u62ccu5408u8bbeu5907
    public static final String TYPE_FORMING = "FORMING";    // u5236u4ef6u8bbeu5907
    public static final String TYPE_TESTING = "TESTING";    // u5b9eu9a8cu8bbeu5907
    public static final String TYPE_SIEVING = "SIEVING";    // u7b5bu5206u8bbeu5907
    
    private SupportedDeviceService() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }
    
    public static synchronized SupportedDeviceService getInstance() {
        if (instance == null) {
            instance = new SupportedDeviceService();
        }
        return instance;
    }
    
    /**
     * u83b7u53d6u6240u6709u8bbeu5907u7c7bu578b
     */
    public List<String> getDeviceTypes() {
        List<String> types = new ArrayList<>();
        types.add(TYPE_MIXING);
        types.add(TYPE_FORMING);
        types.add(TYPE_TESTING);
        types.add(TYPE_SIEVING);
        return types;
    }
    
    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu7684u6240u6709u5382u5546
     */
    public LiveData<List<String>> getManufacturers(String type) {
        MutableLiveData<List<String>> data = new MutableLiveData<>();
        
        // u5148u68c0u67e5u7f13u5b58
        if (manufacturerCache.containsKey(type)) {
            data.setValue(manufacturerCache.get(type));
            return data;
        }
        
        apiService.getManufacturersByType(type).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> manufacturers = response.body().getData();
                    manufacturerCache.put(type, manufacturers); // u7f13u5b58
                    data.setValue(manufacturers);
                } else {
                    // u5982u679cu8bf7u6c42u5931u8d25uff0cu8fd4u56deu7a7au5217u8868
                    Log.e(TAG, "Failed to get manufacturers: " + (response.body() != null ? response.body().getMessage() : "No response"));
                    data.setValue(new ArrayList<>());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                Log.e(TAG, "Network error when getting manufacturers", t);
                data.setValue(new ArrayList<>());
            }
        });
        
        return data;
    }
    
    /**
     * u83b7u53d6u7279u5b9au7c7bu578bu548cu5382u5546u7684u578bu53f7
     */
    public LiveData<List<String>> getModels(String type, String manufacturer) {
        MutableLiveData<List<String>> data = new MutableLiveData<>();
        
        // u751fu6210u7f13u5b58u5bc6u94a5
        String cacheKey = type + "_" + manufacturer;
        
        // u5148u68c0u67e5u7f13u5b58
        if (modelCache.containsKey(cacheKey)) {
            data.setValue(modelCache.get(cacheKey));
            return data;
        }
        
        apiService.getModelsByTypeAndManufacturer(type, manufacturer).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> models = response.body().getData();
                    modelCache.put(cacheKey, models); // u7f13u5b58
                    data.setValue(models);
                } else {
                    // u5982u679cu8bf7u6c42u5931u8d25uff0cu8fd4u56deu7a7au5217u8868
                    Log.e(TAG, "Failed to get models: " + (response.body() != null ? response.body().getMessage() : "No response"));
                    data.setValue(new ArrayList<>());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                Log.e(TAG, "Network error when getting models", t);
                data.setValue(new ArrayList<>());
            }
        });
        
        return data;
    }
    
    /**
     * u83b7u53d6u6240u6709u8bbeu5907
     */
    public LiveData<List<SupportedDeviceResponse>> getAllDevices() {
        MutableLiveData<List<SupportedDeviceResponse>> data = new MutableLiveData<>();
        
        // u5982u679cu7f13u5b58u4e2du5df2u7ecfu6709u6570u636euff0cu76f4u63a5u8fd4u56de
        if (!allDevicesCache.isEmpty()) {
            data.setValue(allDevicesCache);
            return data;
        }
        
        apiService.getAllSupportedDevices().enqueue(new Callback<ApiResponse<List<SupportedDeviceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SupportedDeviceResponse>>> call, Response<ApiResponse<List<SupportedDeviceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SupportedDeviceResponse> devices = response.body().getData();
                    allDevicesCache.clear();
                    allDevicesCache.addAll(devices);
                    data.setValue(devices);
                } else {
                    Log.e(TAG, "Failed to get all devices: " + (response.body() != null ? response.body().getMessage() : "No response"));
                    data.setValue(new ArrayList<>());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<SupportedDeviceResponse>>> call, Throwable t) {
                Log.e(TAG, "Network error when getting all devices", t);
                data.setValue(new ArrayList<>());
            }
        });
        
        return data;
    }
    
    /**
     * u67e5u627eu7279u5b9au7c7bu578bu3001u5382u5546u548cu578bu53f7u7684u8bbeu5907
     */
    public LiveData<SupportedDeviceResponse> findMatchingDevice(String type, String manufacturer, String model) {
        MutableLiveData<SupportedDeviceResponse> data = new MutableLiveData<>();
        
        // u5982u679cu7f13u5b58u4e2du5df2u6709u6570u636euff0cu5c1du8bd5u5339u914d
        if (!allDevicesCache.isEmpty()) {
            for (SupportedDeviceResponse device : allDevicesCache) {
                if (device.getType().equals(type) && 
                    device.getManufacturer().equals(manufacturer) && 
                    device.getModel().equals(model)) {
                    data.setValue(device);
                    return data;
                }
            }
        }
        
        // u5982u679cu7f13u5b58u4e2du6ca1u6709uff0cu5219u83b7u53d6u7406u4f5cu7c7bu578bu7684u6240u6709u8bbeu5907
        apiService.getSupportedDevicesByType(type).enqueue(new Callback<ApiResponse<List<SupportedDeviceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SupportedDeviceResponse>>> call, Response<ApiResponse<List<SupportedDeviceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SupportedDeviceResponse> devices = response.body().getData();
                    SupportedDeviceResponse matchedDevice = null;
                    
                    for (SupportedDeviceResponse device : devices) {
                        if (device.getManufacturer().equals(manufacturer) && device.getModel().equals(model)) {
                            matchedDevice = device;
                            break;
                        }
                    }
                    
                    data.setValue(matchedDevice);
                } else {
                    Log.e(TAG, "Failed to find matching device: " + (response.body() != null ? response.body().getMessage() : "No response"));
                    data.setValue(null);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<SupportedDeviceResponse>>> call, Throwable t) {
                Log.e(TAG, "Network error when finding matching device", t);
                data.setValue(null);
            }
        });
        
        return data;
    }
    
    /**
     * u6e05u7a7au7f13u5b58
     */
    public void clearCache() {
        manufacturerCache.clear();
        modelCache.clear();
        allDevicesCache.clear();
    }
}
