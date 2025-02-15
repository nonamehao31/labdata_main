package com.example.labdata_main.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.constants.EquipmentConstants;
import com.google.gson.annotations.SerializedName;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceInfo implements Parcelable {
    // 设备类型常量，使用 EquipmentConstants 中的定义
    public static final String TYPE_MIXING = EquipmentConstants.TYPE_MIXING;      // 搅拌机
    public static final String TYPE_FORMING = EquipmentConstants.TYPE_FORMING;    // 成型仪
    public static final String TYPE_TESTING = EquipmentConstants.TYPE_TESTING;    // 试验仪

    @SerializedName("type")
    private String type;
    
    @SerializedName("manufacturer")
    private String manufacturer;
    
    @SerializedName("model")
    private String model;
    
    @SerializedName("purchaseYear")
    private String purchaseYear;

    private String name;
    private String deviceId;

    private transient ExecutorService executorService;

    public DeviceInfo() {
        initExecutorService();
    }

    public DeviceInfo(String name, String deviceId, String type) {
        this.name = name;
        this.deviceId = deviceId;
        this.type = type;
        initExecutorService();
    }

    public DeviceInfo(String deviceId, String type, String manufacturer, String model, String purchaseYear, String companyId) {
        this.deviceId = deviceId;
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.purchaseYear = purchaseYear;
        this.name = String.format("%s %s", manufacturer, model);
        initExecutorService();
    }

    private void initExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
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

    @Override
    public int describeContents() {
        return 0;
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

    /**
     * 获取设备类型的中文名称
     */
    public String getTypeDisplayName() {
        return EquipmentConstants.getTypeDisplayName(type);
    }

    /**
     * 异步验证设备信息是否符合要求
     * @param context 上下文
     * @return LiveData<Boolean> 验证结果
     */
    public LiveData<Boolean> validateDeviceAsync(Context context) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // 验证设备类型
        if (type == null || !(type.equals(TYPE_MIXING) || 
                            type.equals(TYPE_FORMING) || 
                            type.equals(TYPE_TESTING))) {
            result.setValue(false);
            return result;
        }

        // 验证必要字段是否存在
        if (manufacturer == null || model == null || purchaseYear == null) {
            result.setValue(false);
            return result;
        }

        // 在数据库中异步查找匹配的设备
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                Device matchingDevice = db.deviceDao().findMatchingDevice(
                    type, manufacturer, model, purchaseYear);
                result.postValue(matchingDevice != null);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    /**
     * 获取设备描述信息
     */
    public String getDescription() {
        return String.format("%s\n厂家：%s\n型号：%s\n购买年份：%s", 
            getTypeDisplayName(), manufacturer, model, purchaseYear);
    }

    /**
     * 清理资源
     */
    public void dispose() {
        executorService.shutdown();
    }
}
