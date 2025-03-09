package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.labdata_main.database.Converters;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "mix_ratios")
@TypeConverters({Converters.class})
public class MixRatio implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "creation_time")
    private long creationTime;

    @ColumnInfo(name = "project_id")
    private long projectId;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "materials")
    private List<MaterialItem> materials;

    @ColumnInfo(name = "total_amount")
    private String totalAmount;

    // 默认构造函数，Room 将使用这个
    public MixRatio() {
    }

    protected MixRatio(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        creationTime = in.readLong();
        projectId = in.readLong();
        materials = in.createTypedArrayList(MaterialItem.CREATOR);
        totalAmount = in.readString();
    }

    public static final Creator<MixRatio> CREATOR = new Creator<MixRatio>() {
        @Override
        public MixRatio createFromParcel(Parcel in) {
            return new MixRatio(in);
        }

        @Override
        public MixRatio[] newArray(int size) {
            return new MixRatio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(creationTime);
        dest.writeLong(projectId);
        dest.writeTypedList(materials);
        dest.writeString(totalAmount);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public List<MaterialItem> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialItem> materials) {
        this.materials = materials;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * 获取该配比中所有材料的ID列表
     * @return 材料ID列表
     */
    public List<Long> getMaterialIds() {
        List<Long> ids = new ArrayList<>();
        if (materials != null) {
            for (MaterialItem material : materials) {
                ids.add(material.getId());
            }
        }
        return ids;
    }
}