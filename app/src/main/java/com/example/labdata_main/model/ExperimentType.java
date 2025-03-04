package com.example.labdata_main.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "experiment_types")
public class ExperimentType {
    // 实验类型常量
    public static final String TYPE_DENSITY = "density";
    public static final String TYPE_PENETRATION = "penetration";
    public static final String TYPE_DUCTILITY = "ductility";
    public static final String TYPE_SOFTENING_POINT = "softening_point";
    public static final String TYPE_TFOT = "tfot";
    public static final String TYPE_RTFOT = "rtfot";
    public static final String TYPE_FLASH_POINT = "flash_point";
    public static final String TYPE_VISCOSITY = "viscosity";
    public static final String TYPE_BBR = "bbr";
    public static final String TYPE_DSR = "dsr";
    public static final String TYPE_DTT = "dtt";
    public static final String TYPE_PAV = "pav";
    public static final String TYPE_MSCR = "mscr";
    public static final String TYPE_FORCE_DUCTILITY = "force_ductility";
    public static final String TYPE_BROOKFIELD_VISCOSITY = "brookfield_viscosity";
    public static final String TYPE_MIXTURE_BENDING = "mixture_bending";
    public static final String TYPE_DYNAMIC_MODULUS = "dynamic_modulus";
    public static final String TYPE_DIRECT_STRETCHING_FATIGUE = "direct_stretching_fatigue";
    public static final String TYPE_FOUR_POINT_BENDING = "four_point_bending";
    public static final String TYPE_SINGLE_AXIS_COMPRESSION = "single_axis_compression";
    public static final String TYPE_MIX_SPLITTING = "mix_splitting";

    // 实验类别常量
    public static final String CATEGORY_ASPHALT = "ASPHALT";
    public static final String CATEGORY_MIXTURE = "MIXTURE";

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    @NonNull
    @ColumnInfo(name = "category")
    private String category;

    public ExperimentType(@NonNull String name, @NonNull String type, @NonNull String category) {
        this.name = name;
        this.type = type;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentType that = (ExperimentType) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
