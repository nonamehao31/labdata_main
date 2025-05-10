package com.example.labdata_main.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mix_designs")
public class MixDesign {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String materialType; // 材料类型
    public String materialName; // 材料名称
    public String materialCode; // 材料编号
    public String gradation;   // 级配
    public float percentage;   // 配比百分比
    public long timestamp;     // 创建时间戳
    public int designGroup;    // 配比方案组号
}
