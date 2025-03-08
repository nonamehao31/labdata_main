package com.example.labdata_main.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.labdata_main.constants.EquipmentConstants;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.service.SupportedDeviceService;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 设备信息类，包含设备类型、制造商、型号和购买年份
 */
public class DeviceInfo implements Parcelable {
    private static final String TAG = "DeviceInfo";
    
    // 设备类型常量，与SupportedDeviceService中保持一致
    public static final String TYPE_MIXING = SupportedDeviceService.TYPE_MIXING;      // 拌合设备
    public static final String TYPE_FORMING = SupportedDeviceService.TYPE_FORMING;    // 制件设备
    public static final String TYPE_TESTING = SupportedDeviceService.TYPE_TESTING;    // 实验设备
    public static final String TYPE_SIEVING = SupportedDeviceService.TYPE_SIEVING;    // 筛分设备
    
    private String type;
    private String manufacturer;
    private String model;
    private String purchaseYear;
    private String name;
    private String deviceId;
    
    private ExecutorService executorService;

    private void initExecutorService() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public DeviceInfo() {
        initExecutorService();
    }

    public DeviceInfo(String type, String manufacturer, String model, String purchaseYear) {
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.purchaseYear = purchaseYear;
        initExecutorService();
    }
    
    public DeviceInfo(String type, String manufacturer, String model, String purchaseYear, String name, String deviceId) {
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.purchaseYear = purchaseYear;
        this.name = name;
        this.deviceId = deviceId;
        initExecutorService();
    }

    protected DeviceInfo(Parcel in) {
        type = in.readString();
        manufacturer = in.readString();
        model = in.readString();
        purchaseYear = in.readString();
        name = in.readString();
        deviceId = in.readString();
        initExecutorService();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(manufacturer);
        dest.writeString(model);
        dest.writeString(purchaseYear);
        dest.writeString(name);
        dest.writeString(deviceId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPurchaseYear() {
        return purchaseYear;
    }

    public void setPurchaseYear(String purchaseYear) {
        this.purchaseYear = purchaseYear;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * 释放资源
     */
    public void dispose() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 验证设备信息是否有效
     * @return 如果设备信息有效返回true，否则返回false
     */
    public boolean validateDevice() {
        // 检查设备类型是否有效
        if (TextUtils.isEmpty(type) || !isValidType(type)) {
            return false;
        }

        // 检查制造商是否有效
        if (TextUtils.isEmpty(manufacturer)) {
            return false;
        }

        // 检查型号是否有效
        if (TextUtils.isEmpty(model)) {
            return false;
        }

        // 检查购买年份是否有效
        return !TextUtils.isEmpty(purchaseYear);
    }

    /**
     * 异步验证设备信息
     * @param context 上下文
     * @return 包含验证结果的LiveData
     */
    public LiveData<Boolean> validateDeviceAsync(Context context) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // 基本验证
        if (!validateDevice()) {
            result.setValue(false);
            return result;
        }

        // 检查数据库中是否已存在匹配的设备
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                Device matchingDevice = db.deviceDao().findMatchingDevice(type, manufacturer, model, purchaseYear);
                result.postValue(matchingDevice == null); // 如果没有匹配的设备，返回true
            } catch (Exception e) {
                Log.e(TAG, "Error validating device async", e);
                result.postValue(false);
            }
        });

        return result;
    }
    
    /**
     * 获取设备类型的显示名称
     * @return 设备类型的显示名称
     */
    public String getTypeDisplayName() {
        // 保留原有功能，但使用新的常量定义
        if (TYPE_MIXING.equals(type)) {
            return "搅拌设备";
        } else if (TYPE_FORMING.equals(type)) {
            return "制件设备";
        } else if (TYPE_TESTING.equals(type)) {
            return "实验设备";
        } else if (TYPE_SIEVING.equals(type)) {
            return "筛分设备";
        }
        return type;
    }

    /**
     * 检查设备类型是否有效
     * @param type 设备类型
     * @return 如果类型有效返回true，否则返回false
     */
    private boolean isValidType(String type) {
        // 使用支持设备服务中定义的设备类型常量
        return type.equals(TYPE_MIXING) ||
               type.equals(TYPE_FORMING) ||
               type.equals(TYPE_TESTING) ||
               type.equals(TYPE_SIEVING);
    }

    /**
     * 获取设备描述
     * @return 设备描述字符串
     */
    public String getDescription() {
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(manufacturer) || TextUtils.isEmpty(model)) {
            return "";
        }
        return String.format("%s\n厂家：%s\n型号：%s\n购买年份：%s", 
                getTypeDisplayName(), manufacturer, model, purchaseYear);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(model, that.model) &&
                Objects.equals(purchaseYear, that.purchaseYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, manufacturer, model, purchaseYear);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "type='" + type + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", purchaseYear='" + purchaseYear + '\'' +
                '}';
    }
}
