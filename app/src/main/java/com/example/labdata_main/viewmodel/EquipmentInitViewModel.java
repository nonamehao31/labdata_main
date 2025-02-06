package com.example.labdata_main.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.Device;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EquipmentInitViewModel extends AndroidViewModel {
    private final ExecutorService executorService;
    private final AppDatabase database;

    public EquipmentInitViewModel(Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(application);
    }

    public LiveData<Boolean> saveDevices(List<Device> devices) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        executorService.execute(() -> {
            try {
                // 保存所有设备信息
                for (Device device : devices) {
                    database.deviceDao().insert(device);
                }
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        
        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
