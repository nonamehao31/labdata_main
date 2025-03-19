package com.example.labdata_main.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;

/**
 * Gson工具类，提供统一的Gson实例创建方法
 * 避免在应用中重复配置Gson实例，提高代码复用性
 */
public class GsonUtil {
    
    private static Gson gsonInstance;
    
    /**
     * 获取预配置的Gson实例，已处理常见的序列化/反序列化问题
     * @return 配置好的Gson实例
     */
    public static Gson getGson() {
        if (gsonInstance == null) {
            gsonInstance = new GsonBuilder()
                // 处理ExecutorService接口无法被实例化的问题
                .registerTypeAdapter(ExecutorService.class, 
                    (JsonDeserializer<ExecutorService>) (json, typeOfT, context) -> null)
                // 添加更多类型适配器...
                // 如果需要序列化被标记为transient的字段，可以启用下面的选项
                // .excludeFieldsWithModifiers(Modifier.STATIC)  // 只排除static字段，默认会排除transient字段
                .serializeNulls() // 序列化null值
                .disableHtmlEscaping() // 禁用HTML转义
                .setPrettyPrinting() // 美化输出
                .create();
        }
        return gsonInstance;
    }
    
    /**
     * 获取带有自定义配置的Gson构建器
     * @return 预配置的GsonBuilder实例
     */
    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
            .registerTypeAdapter(ExecutorService.class, 
                (JsonDeserializer<ExecutorService>) (json, typeOfT, context) -> null);
    }
    
    /**
     * 将JSON字符串转换为对象
     * @param json JSON字符串
     * @param classOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }
    
    /**
     * 将JSON字符串转换为对象
     * @param json JSON字符串
     * @param typeOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return getGson().fromJson(json, typeOfT);
    }
    
    /**
     * 将对象转换为JSON字符串
     * @param src 源对象
     * @return JSON字符串
     */
    public static String toJson(Object src) {
        return getGson().toJson(src);
    }
}
