package com.example.labdata_main.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "specimens",
        foreignKeys = @ForeignKey(
                entity = MixRatio.class,
                parentColumns = "id",
                childColumns = "mix_ratio_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("mix_ratio_id")})
public class Specimen {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "mix_ratio_id")
    private long mixRatioId;

    @ColumnInfo(name = "mixing_temperature")
    private float mixingTemperature;

    @ColumnInfo(name = "mixing_speed")
    private float mixingSpeed;

    @ColumnInfo(name = "compaction_method", defaultValue = "")
    private String compactionMethod;

    @ColumnInfo(name = "creation_time")
    private long creationTime;

    @ColumnInfo(name = "cut_shape")
    private String cutShape;

    @ColumnInfo(name = "cut_count", defaultValue = "1")
    private int cutCount;

    @ColumnInfo(name = "length", defaultValue = "0")
    private float length;

    @ColumnInfo(name = "width", defaultValue = "0")
    private float width;

    @ColumnInfo(name = "height", defaultValue = "0")
    private float height;

    @ColumnInfo(name = "radius", defaultValue = "0")
    private float radius;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(long mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public float getMixingTemperature() {
        return mixingTemperature;
    }

    public void setMixingTemperature(float mixingTemperature) {
        this.mixingTemperature = mixingTemperature;
    }

    public float getMixingSpeed() {
        return mixingSpeed;
    }

    public void setMixingSpeed(float mixingSpeed) {
        this.mixingSpeed = mixingSpeed;
    }

    public String getCompactionMethod() {
        return compactionMethod;
    }

    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getCutShape() {
        return cutShape;
    }

    public void setCutShape(String cutShape) {
        this.cutShape = cutShape;
    }

    public int getCutCount() {
        return cutCount;
    }

    public void setCutCount(int cutCount) {
        this.cutCount = cutCount;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
