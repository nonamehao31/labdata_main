package com.example.labdata_main.api;

/**
 * API配置类，存储API相关的常量
 */
public class ApiConfig {
    // 服务器API基础URL，确保使用正确的IP地址和端口
    // 使用计算机实际IP地址
    public static final String BASE_URL = "http://10.11.232.216:8080/api/";
    // 备选方案(如果上面的不工作，可以尝试):
    // public static final String BASE_URL = "http://192.168.137.1:8080/api/";
    // public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    
    // API路径
    public static final String LOGIN_URL = "auth/login";
    public static final String REGISTER_URL = "auth/register";
    
    // 超时设置（单位：秒）
    public static final int CONNECT_TIMEOUT = 60; // 增加超时时间
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
}
