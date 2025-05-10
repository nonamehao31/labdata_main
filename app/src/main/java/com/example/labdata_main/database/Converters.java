package com.example.labdata_main.database;

import androidx.room.TypeConverter;
import com.example.labdata_main.model.MaterialItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromMaterialList(List<MaterialItem> materials) {
        if (materials == null) {
            return null;
        }
        return gson.toJson(materials);
    }

    @TypeConverter
    public static List<MaterialItem> toMaterialList(String materialsString) {
        if (materialsString == null) {
            return null;
        }
        Type listType = new TypeToken<List<MaterialItem>>() {}.getType();
        return gson.fromJson(materialsString, listType);
    }
}
