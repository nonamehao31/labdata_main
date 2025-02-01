package com.example.labdata_main.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "molding_methods")
public class MoldingMethod {
    public static final int TYPE_CUBE = 1;
    public static final int TYPE_CYLINDER = 2;
    public static final int TYPE_PRISM = 3;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "size")
    private float size;        // 立方体边长

    @ColumnInfo(name = "diameter")
    private float diameter;    // 圆柱体直径

    @ColumnInfo(name = "length")
    private float length;      // 棱柱体长度

    @ColumnInfo(name = "width")
    private float width;       // 棱柱体宽度

    @ColumnInfo(name = "height")
    private float height;      // 圆柱体/棱柱体高度

    @ColumnInfo(name = "count")
    private int count;        // 试块数量

    @ColumnInfo(name = "mixing_temperature")
    private float mixingTemperature;

    @ColumnInfo(name = "mixing_speed")
    private float mixingSpeed;

    @ColumnInfo(name = "mixing_time")
    private float mixingTime;

    @ColumnInfo(name = "compaction_method")
    private String compactionMethod;

    public MoldingMethod() {}

    @Ignore
    public MoldingMethod(float mixingTemperature, float mixingSpeed, float mixingTime, String compactionMethod) {
        this.mixingTemperature = mixingTemperature;
        this.mixingSpeed = mixingSpeed;
        this.mixingTime = mixingTime;
        this.compactionMethod = compactionMethod;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public float getSize() { return size; }
    public void setSize(float size) { this.size = size; }

    public float getDiameter() { return diameter; }
    public void setDiameter(float diameter) { this.diameter = diameter; }

    public float getLength() { return length; }
    public void setLength(float length) { this.length = length; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public float getMixingTemperature() { return mixingTemperature; }
    public void setMixingTemperature(float mixingTemperature) { this.mixingTemperature = mixingTemperature; }

    public float getMixingSpeed() { return mixingSpeed; }
    public void setMixingSpeed(float mixingSpeed) { this.mixingSpeed = mixingSpeed; }

    public float getMixingTime() { return mixingTime; }
    public void setMixingTime(float mixingTime) { this.mixingTime = mixingTime; }

    public String getCompactionMethod() { return compactionMethod; }
    public void setCompactionMethod(String compactionMethod) { this.compactionMethod = compactionMethod; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // 试块尺寸信息
        switch (type) {
            case TYPE_CUBE:
                sb.append(String.format("立方体试块 %.0f×%.0f×%.0f mm", size, size, size));
                break;
            case TYPE_CYLINDER:
                sb.append(String.format("圆柱体试块 Φ%.0f×%.0f mm", diameter, height));
                break;
            case TYPE_PRISM:
                sb.append(String.format("棱柱体试块 %.0f×%.0f×%.0f mm", length, width, height));
                break;
        }
        sb.append(String.format(" %d个", count));

        // 添加拌合参数信息，使用标识和单位
        sb.append("|temp=").append(String.format("%.1f", mixingTemperature));  // 拌合温度
        sb.append("|speed=").append(String.format("%.1f", mixingSpeed));       // 拌合速度
        sb.append("|time=").append(String.format("%.1f", mixingTime));         // 拌合时间
        sb.append("|method=").append(compactionMethod);                        // 压实方式

        return sb.toString();
    }
}
