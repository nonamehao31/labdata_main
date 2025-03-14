package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MaterialItem implements Parcelable {
    private String name;
    private float percentage;
    private String type;
    private String materialName;
    private String amount;
    private Long materialId; // 新增字段，用于存储材料的后端数据库ID

    public MaterialItem() {
    }

    public MaterialItem(String name, float percentage, String type) {
        this.name = name;
        this.percentage = percentage;
        this.type = type;
    }

    public MaterialItem(String name, float percentage, String type, Long materialId) {
        this.name = name;
        this.percentage = percentage;
        this.type = type;
        this.materialId = materialId;
    }

    public MaterialItem(String materialName, String amount) {
        this.materialName = materialName;
        this.amount = amount;
    }

    protected MaterialItem(Parcel in) {
        name = in.readString();
        percentage = in.readFloat();
        type = in.readString();
        materialName = in.readString();
        amount = in.readString();
        if (in.readByte() == 0) {
            materialId = null;
        } else {
            materialId = in.readLong();
        }
    }

    public static final Creator<MaterialItem> CREATOR = new Creator<MaterialItem>() {
        @Override
        public MaterialItem createFromParcel(Parcel in) {
            return new MaterialItem(in);
        }

        @Override
        public MaterialItem[] newArray(int size) {
            return new MaterialItem[size];
        }
    };

    public String getName() {
        return name != null ? name : materialName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
    
    /**
     * 获取材料ID (为了兼容现有代码)
     * @return 材料ID，如果为空则返回0
     */
    public long getId() {
        return materialId != null ? materialId : 0L;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(percentage);
        dest.writeString(type);
        dest.writeString(materialName);
        dest.writeString(amount);
        if (materialId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(materialId);
        }
    }
}
