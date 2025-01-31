package com.example.labdata_main;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.example.labdata_main.model.Equipment;

public class SharedPrefsManager {
    private static final String PREFS_NAME = "LabDataPrefs";
    private static final String KEY_EQUIPMENT_LIST = "equipment_list";
    private static final String KEY_INITIALIZED = "initialized";
    private static final String KEY_USER_TYPE = "user_type";  // 添加用户类型的key
    private static final String KEY_USER_NAME = "user_name";  // 添加用户名的key

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveEquipmentList(List<Equipment> equipmentList) {
        String json = gson.toJson(equipmentList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EQUIPMENT_LIST, json);
        editor.apply();
    }

    public ArrayList<Equipment> getEquipmentList() {
        String json = sharedPreferences.getString(KEY_EQUIPMENT_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Equipment>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setInitialized(boolean initialized) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_INITIALIZED, initialized);
        editor.apply();
    }

    public boolean isInitialized() {
        return sharedPreferences.getBoolean(KEY_INITIALIZED, false);
    }

    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void saveUserLoginSession(int id, String email, String name, String company, String phone, int userType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id);
        editor.putString("email", email);
        editor.putString("name", name);
        editor.putString("company", company);
        editor.putString("phone", phone);
        editor.putInt(KEY_USER_TYPE, userType);  // 保存用户类型
        editor.apply();
    }

    public void setUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString("email", "");
    }

    public String getUserPhone() {
        return sharedPreferences.getString("phone", "");
    }

    public String getUserCompany() {
        return sharedPreferences.getString("company", "");
    }

    public int getUserId() {
        return sharedPreferences.getInt("id", -1);
    }

    public int getUserType() {
        return sharedPreferences.getInt(KEY_USER_TYPE, 0);  // 默认返回0（实验员）
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }
}
