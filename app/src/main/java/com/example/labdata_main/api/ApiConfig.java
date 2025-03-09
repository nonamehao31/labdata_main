package com.example.labdata_main.api;

/**
 * API配置类，包含所有API端点URL
 */
public class ApiConfig {
    // API基础路径
    public static final String BASE_URL = "http://10.11.232.216:8080/";
    
    // 认证相关
    public static final String LOGIN_URL = "auth/login";
    public static final String REGISTER_URL = "auth/register";
    public static final String CHECK_EMAIL_EXISTS_URL = "auth/check-email"; // 新增
    
    // 设备相关
    public static final String SAVE_DEVICE_URL = "devices/save";
    public static final String SAVE_DEVICES_BATCH_URL = "devices/save-batch";
    public static final String GET_DEVICES_BY_COMPANY_URL = "devices/company/{companyId}";
    public static final String GET_USER_DEVICES_URL = "devices/user";
    
    // 获取设备的初始化状态
    public static final String CHECK_COMPANY_EQUIPMENT_INITIALIZED = "devices/company/{companyId}/initialized";
    
    // 支持设备相关
    public static final String GET_ALL_SUPPORTED_DEVICES_URL = "supported-devices";
    public static final String GET_SUPPORTED_DEVICES_BY_TYPE_URL = "supported-devices/type/{type}";
    public static final String GET_MANUFACTURERS_BY_TYPE_URL = "supported-devices/type/{type}/manufacturers";
    public static final String GET_MODELS_BY_TYPE_AND_MANUFACTURER_URL = "supported-devices/type/{type}/manufacturer/{manufacturer}/models";
    
    // 实验任务相关
    public static final String SAVE_EXPERIMENT_TASK_URL = "api/experiment-tasks";
    public static final String GET_EXPERIMENT_TASKS_URL = "api/experiment-tasks";
    public static final String GET_EXPERIMENT_TASK_BY_ID_URL = "api/experiment-tasks/{id}";
    public static final String UPDATE_EXPERIMENT_TASK_URL = "api/experiment-tasks/{id}";
    public static final String SYNC_EXPERIMENT_TASK_URL = "api/experiment-tasks/sync";
    public static final String SYNC_EXPERIMENT_TASKS_URL = "experiment-tasks/sync";
    
    // 沥青实验相关
    public static final String CREATE_ASPHALT_TASK_URL = "api/asphalt/tasks";
    public static final String GET_ASPHALT_TASKS_URL = "api/asphalt/tasks";
    public static final String GET_ASPHALT_TASK_BY_ID_URL = "api/asphalt/tasks/{id}";
    public static final String UPDATE_ASPHALT_TASK_URL = "api/asphalt/tasks/{id}";
    
    // 超时设置（单位：秒）
    public static final int CONNECT_TIMEOUT = 60; 
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
}
