package com.example.labdata_main.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.request.DeviceRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipmentInitViewModel extends AndroidViewModel {
    private final ExecutorService executorService;
    private final AppDatabase database;
    private final ApiService apiService;
    private static final String TAG = "EquipmentInitViewModel";

    public EquipmentInitViewModel(Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(application);
        apiService = ApiClient.getApiService();
    }

    public LiveData<Boolean> saveDevices(List<Device> devices) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        executorService.execute(() -> {
            try {
                // 保存到本地数据库
                for (Device device : devices) {
                    database.deviceDao().insert(device);
                }
                
                // 同时发送到后端服务器
                sendDevicesToServer(devices, result);
            } catch (Exception e) {
                Log.e(TAG, "Error saving devices locally: " + e.getMessage(), e);
                result.postValue(false);
            }
        });
        
        return result;
    }
    
    private void sendDevicesToServer(List<Device> devices, MutableLiveData<Boolean> result) {
        List<DeviceRequest> requests = new ArrayList<>();
        for (Device device : devices) {
            requests.add(new DeviceRequest(device));
        }
        
        // 打印身份验证信息以进行调试
        SharedPrefsManager prefsManager = new SharedPrefsManager(getApplication());
        String authHeader = prefsManager.getAuthHeader();
        Log.d(TAG, "Sending request with auth header: " + (authHeader != null ? "[Token present]" : "[No token]"));
        
        apiService.saveDevicesBatch(requests).enqueue(new Callback<ApiResponse<List<DeviceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DeviceResponse>>> call, Response<ApiResponse<List<DeviceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Devices saved to server successfully");
                    result.postValue(true);
                } else {
                    // 详细记录错误原因
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Unable to read error body", e);
                    }
                    
                    Log.w(TAG, "Server error: Code " + response.code() + ", Message: " + 
                          (response.message() != null ? response.message() : "No message") +
                          ", Body: " + errorBody);
                    
                    // 如果服务器响应失败但本地保存已完成，仍然返回成功
                    Log.w(TAG, "Server response not successful but local save is complete, returning success");
                    result.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DeviceResponse>>> call, Throwable t) {
                // 网络错误但本地保存已完成，仍然返回成功
                Log.e(TAG, "Network error while saving devices to server: " + t.getMessage(), t);
                result.postValue(true);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
