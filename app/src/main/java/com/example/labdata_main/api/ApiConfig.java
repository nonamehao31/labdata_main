package com.example.labdata_main.api;

/**
 * API配置类，存储API相关的常量
 */
public class ApiConfig {
    // 服务器API基础URL，确保使用正确的IP地址和端口
    // 使用计算机实际IP地址
    //public static final String BASE_URL = "http://10.11.232.216:8080/";
    // 备选方案(如果上面的不工作，可以尝试):
     public static final String BASE_URL = "http://192.168.43.115:8080/";
     //public static final String BASE_URL = "http://10.0.2.2:8080/";
    
    // API路径
    public static final String LOGIN_URL = "auth/login";
    public static final String REGISTER_URL = "auth/register";
    public static final String SAVE_DEVICE_URL = "devices";
    public static final String SAVE_DEVICES_BATCH_URL = "devices/batch";
    public static final String GET_DEVICES_BY_COMPANY_URL = "devices/company/{companyId}";
    public static final String GET_USER_DEVICES_URL = "devices";
    
    // 新增的设备支持API路径
    public static final String GET_ALL_SUPPORTED_DEVICES_URL = "supported-devices";
    public static final String GET_SUPPORTED_DEVICES_BY_TYPE_URL = "supported-devices/type/{type}";
    public static final String GET_MANUFACTURERS_BY_TYPE_URL = "supported-devices/manufacturers/{type}";
    public static final String GET_MODELS_BY_TYPE_AND_MANUFACTURER_URL = "supported-devices/models/{type}/{manufacturer}";
    
    // 超时设置（单位：秒）
    public static final int CONNECT_TIMEOUT = 60; // 增加超时时间
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
}
