package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MaterialItem implements Parcelable {
    private String name;
    private float percentage;
    private String type;
    private String materialName;
    private String amount;

    public MaterialItem() {
    }

    public MaterialItem(String name, float percentage, String type) {
        this.name = name;
        this.percentage = percentage;
        this.type = type;
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

    // 添加getId()方法用于获取材料ID
    public long getId() {
        // 由于MaterialItem没有ID字段，这里返回一个基于名称的哈希码作为临时ID
        // 实际生产环境中，应当添加一个真实的ID字段
        return getName() != null ? getName().hashCode() : 0;
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
    }
}
