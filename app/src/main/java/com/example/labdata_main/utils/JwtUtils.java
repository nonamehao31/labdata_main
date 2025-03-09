package com.example.labdata_main.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * JWT令牌工具类
 * 用于解析JWT令牌并获取信息
 */
public class JwtUtils {

    private static final String TAG = "JwtUtils";

    /**
     * 解析JWT令牌
     * 注意：这只是一个简单的客户端解析工具，不做签名验证
     * @param jwtToken JWT令牌字符串
     */
    public static void decodeAndLogJwt(String jwtToken) {
        try {
            // 移除Bearer前缀（如果有）
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }
            
            Log.d(TAG, "解析JWT令牌: " + jwtToken);
            
            // 使用字符串索引和substring方法手动分割，避免正则表达式的问题
            int firstDotIndex = jwtToken.indexOf('.');
            int secondDotIndex = jwtToken.indexOf('.', firstDotIndex + 1);
            
            if (firstDotIndex == -1 || secondDotIndex == -1) {
                Log.e(TAG, "无效的JWT格式，找不到分隔符: 第一个点: " + firstDotIndex + ", 第二个点: " + secondDotIndex);
                return;
            }
            
            String header = jwtToken.substring(0, firstDotIndex);
            String payload = jwtToken.substring(firstDotIndex + 1, secondDotIndex);
            String signature = jwtToken.substring(secondDotIndex + 1);
            
            Log.d(TAG, "JWT分段成功 - 头部长度: " + header.length() + ", 载荷长度: " + payload.length() + ", 签名长度: " + signature.length());
            
            // 解析载荷部分（payload）
            String decoded = getJson(payload);
            
            JSONObject jsonObject = new JSONObject(decoded);
            
            // 获取常见信息
            String subject = jsonObject.optString("sub", "无");
            long issuedAt = jsonObject.optLong("iat", 0) * 1000; // 转换为毫秒
            long expiration = jsonObject.optLong("exp", 0) * 1000; // 转换为毫秒
            
            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String issuedAtFormatted = issuedAt > 0 ? sdf.format(new Date(issuedAt)) : "未知";
            String expirationFormatted = expiration > 0 ? sdf.format(new Date(expiration)) : "未知";
            
            // 计算距离过期时间
            long currentTime = System.currentTimeMillis();
            long timeUntilExpiry = expiration - currentTime;
            
            // 输出日志
            Log.d(TAG, "用户ID: " + subject);
            Log.d(TAG, "颁发时间: " + issuedAtFormatted + " (" + issuedAt + ")");
            Log.d(TAG, "过期时间: " + expirationFormatted + " (" + expiration + ")");
            Log.d(TAG, "当前时间: " + sdf.format(new Date(currentTime)) + " (" + currentTime + ")");
            Log.d(TAG, String.format("距离过期还有: %d 毫秒 (约 %.2f 小时)", 
                    timeUntilExpiry, timeUntilExpiry / (1000.0 * 60 * 60)));
            
            // 输出完整JSON
            Log.d(TAG, "完整载荷: " + decoded);
            
        } catch (JSONException e) {
            Log.e(TAG, "解析JWT JSON失败: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "解析JWT失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Base64解码并转换为JSON字符串
     */
    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
