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

    @ColumnInfo(name = "mix_ratio_id")
    private long mixRatioId;

    @ColumnInfo(name = "method_index")
    private int methodIndex;  // 同一配比下的方法索引

    @ColumnInfo(name = "method_group")
    private String methodGroup;  // 用于标识同一组的制件方法

    public MoldingMethod() {}

    @Ignore
    public MoldingMethod(float mixingTemperature, float mixingSpeed, float mixingTime, String compactionMethod) {
        this.mixingTemperature = mixingTemperature;
        this.mixingSpeed = mixingSpeed;
        this.mixingTime = mixingTime;
        this.compactionMethod = compactionMethod;
    }

    @Ignore
    public MoldingMethod(int type, float size, float diameter, float length, float width, float height,
                        int count, float mixingTemperature, float mixingSpeed, float mixingTime,
                        String compactionMethod, long mixRatioId, int methodIndex, String methodGroup) {
        this.type = type;
        this.size = size;
        this.diameter = diameter;
        this.length = length;
        this.width = width;
        this.height = height;
        this.count = count;
        this.mixingTemperature = mixingTemperature;
        this.mixingSpeed = mixingSpeed;
        this.mixingTime = mixingTime;
        this.compactionMethod = compactionMethod;
        this.mixRatioId = mixRatioId;
        this.methodIndex = methodIndex;
        this.methodGroup = methodGroup;
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

    public long getMixRatioId() { return mixRatioId; }
    public void setMixRatioId(long mixRatioId) { this.mixRatioId = mixRatioId; }

    public int getMethodIndex() { return methodIndex; }
    public void setMethodIndex(int methodIndex) { this.methodIndex = methodIndex; }

    public String getMethodGroup() { return methodGroup; }
    public void setMethodGroup(String methodGroup) { this.methodGroup = methodGroup; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // 只添加拌合和压实参数信息
        sb.append("temp=").append(String.format("%.1f", mixingTemperature));  // 拌合温度
        sb.append("|speed=").append(String.format("%.1f", mixingSpeed));      // 拌合速度
        sb.append("|time=").append(String.format("%.1f", mixingTime));        // 拌合时间
        sb.append("|method=").append(compactionMethod);                       // 压实方式

        return sb.toString();
    }
}
