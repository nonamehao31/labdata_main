package com.example.labdata_main.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

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
    @ColumnInfo(name = "purchase_year")
    private String purchaseYear;  // 购买年份

    @NonNull
    @ColumnInfo(name = "company_id")
    private String companyId;  // 公司ID
    
    @Ignore
    private int indicatorColor = 0xFF4CAF50;  // 设备类型指示条颜色，默认绿色

    // Room 将使用这个构造器
    public Device() {
    }

    // 应用代码将使用这个构造器
    @Ignore
    public Device(@NonNull String id, @NonNull String type, 
                 @NonNull String manufacturer, @NonNull String model, 
                 @NonNull String purchaseYear, @NonNull String companyId) {
        this.id = id;
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.purchaseYear = purchaseYear;
        this.companyId = companyId;
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

    @NonNull
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(@NonNull String companyId) {
        this.companyId = companyId;
    }
    
    /**
     * 获取设备类型指示条颜色
     */
    public int getIndicatorColor() {
        return indicatorColor;
    }

    /**
     * 设置设备类型指示条颜色
     */
    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }
}
