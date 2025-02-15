package com.example.labdata_main.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "experiment_data_fields",
    foreignKeys = @ForeignKey(
        entity = ExperimentType.class,
        parentColumns = "id",
        childColumns = "experiment_type_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("experiment_type_id")}
)
public class ExperimentDataField {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "field_name")
    private String fieldName;  // 字段名称，如"温度"，"位移"等

    @NonNull
    @ColumnInfo(name = "field_key")
    private String fieldKey;   // 字段键，用于存储，如"temperature"，"displacement"等

    @NonNull
    @ColumnInfo(name = "unit")
    private String unit;      // 单位，如"℃"，"mm"等

    @ColumnInfo(name = "precision")
    private double precision; // 精度，如0.1, 0.5等

    @ColumnInfo(name = "experiment_type_id")
    private long experimentTypeId;  // 关联的实验类型ID

    public ExperimentDataField(@NonNull String fieldName, @NonNull String fieldKey,
                             @NonNull String unit, double precision, long experimentTypeId) {
        this.fieldName = fieldName;
        this.fieldKey = fieldKey;
        this.unit = unit;
        this.precision = precision;
        this.experimentTypeId = experimentTypeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(@NonNull String fieldName) {
        this.fieldName = fieldName;
    }

    @NonNull
    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(@NonNull String fieldKey) {
        this.fieldKey = fieldKey;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public long getExperimentTypeId() {
        return experimentTypeId;
    }

    public void setExperimentTypeId(long experimentTypeId) {
        this.experimentTypeId = experimentTypeId;
    }
}
