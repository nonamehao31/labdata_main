package com.example.labdata_main.database;

import androidx.room.TypeConverter;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromMaterialItemList(List<MaterialItem> materials) {
        if (materials == null) {
            return null;
        }
        return gson.toJson(materials);
    }

    @TypeConverter
    public static List<MaterialItem> toMaterialItemList(String materialsJson) {
        if (materialsJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<MaterialItem>>() {}.getType();
        return gson.fromJson(materialsJson, listType);
    }

    @TypeConverter
    public static String fromMixRatioList(List<MixRatio> mixRatios) {
        if (mixRatios == null) {
            return null;
        }
        return gson.toJson(mixRatios);
    }

    @TypeConverter
    public static List<MixRatio> toMixRatioList(String mixRatiosJson) {
        if (mixRatiosJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<MixRatio>>() {}.getType();
        return gson.fromJson(mixRatiosJson, listType);
    }

    @TypeConverter
    public static String fromStringListMap(Map<Long, List<String>> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<Long, List<String>> toStringListMap(String mapJson) {
        if (mapJson == null) {
            return null;
        }
        Type mapType = new TypeToken<Map<Long, List<String>>>() {}.getType();
        return gson.fromJson(mapJson, mapType);
    }

    @TypeConverter
    public static String fromMoldingMethodList(List<MoldingMethod> methods) {
        if (methods == null) {
            return null;
        }
        return gson.toJson(methods);
    }

    @TypeConverter
    public static List<MoldingMethod> toMoldingMethodList(String methodsJson) {
        if (methodsJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<MoldingMethod>>() {}.getType();
        return gson.fromJson(methodsJson, listType);
    }
}
