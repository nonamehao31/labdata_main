package com.example.labdata_main.api;

import okhttp3.MediaType;

/**
 * API配置类，包含了API的基本配置信息和端点URL
 */
public class ApiConfig {
    /**
     * API基础URL
     */
    public static final String BASE_URL = "http://10.11.232.216:8080/";
    
    /**
     * 超时设置（秒）
     */
    public static final int CONNECT_TIMEOUT = 60;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
    
    /**
     * API端点URL
     */
    // 登录注册相关
    public static final String LOGIN_URL = "auth/login";
    public static final String REGISTER_URL = "auth/register";
    public static final String CHECK_EMAIL_EXISTS_URL = "auth/check-email";
    
    // 设备相关
    public static final String SAVE_DEVICE_URL = "devices/save";
    public static final String SAVE_DEVICES_BATCH_URL = "devices/save-batch";
    public static final String GET_DEVICES_BY_COMPANY_URL = "devices/company/{companyId}";
    public static final String GET_USER_DEVICES_URL = "devices/user";
    
    // 公司设备相关
    public static final String CHECK_COMPANY_EQUIPMENT_INITIALIZED = "devices/company/{companyId}/initialized";
    public static final String GET_COMPANY_EQUIPMENT_URL = "devices/company/{companyId}/equipment";
    
    // 支持的设备相关
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
    
    // 项目相关
    public static final String PROJECT_URL = "api/projects";
    public static final String PROJECT_BY_ID_URL = "api/projects/{id}";

    /**
     * JSON媒体类型
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
}
