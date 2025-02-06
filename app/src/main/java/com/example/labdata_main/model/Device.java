package com.example.labdata_main.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Device {
    @PrimaryKey
    @NonNull
    private String id;  // 设备唯一标识符

    @NonNull
    private String type;  // 设备类型：MIXING/FORMING/TEST
    
    @NonNull
    private String manufacturer;  // 厂商
    
    @NonNull
    private String model;  // 型号
    
    @NonNull
    private String purchaseYear;  // 购买年份

    public Device(@NonNull String id, @NonNull String type, 
                 @NonNull String manufacturer, @NonNull String model, 
                 @NonNull String purchaseYear) {
        this.id = id;
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.purchaseYear = purchaseYear;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(@NonNull String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @NonNull
    public String getModel() {
        return model;
    }

    public void setModel(@NonNull String model) {
        this.model = model;
    }

    @NonNull
    public String getPurchaseYear() {
        return purchaseYear;
    }

    public void setPurchaseYear(@NonNull String purchaseYear) {
        this.purchaseYear = purchaseYear;
    }
}
