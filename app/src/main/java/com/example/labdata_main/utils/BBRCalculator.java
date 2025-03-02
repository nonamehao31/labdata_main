package com.example.labdata_main.utils;

import android.util.Log;

/**
 * 沥青弯曲蠕变劲度试验(弯曲梁流变仪法)计算工具类
 */
public class BBRCalculator {
    private static final String TAG = "BBRCalculator";

    /**
     * 计算弯曲蠕变劲度模量
     * 公式: S(t) = (P·L³)/(4·b·h³·δ(t))
     *
     * @param load 施加的恒定荷载 (N)
     * @param beamSpan 梁的跨度 (mm)
     * @param width 试件的宽度 (mm)
     * @param height 试件的高度 (mm)
     * @param deflection 时间为t时的梁中点挠度 (mm)
     * @return 弯曲蠕变劲度模量 (MPa)
     */
    public static double calculateStiffness(double load, double beamSpan, double width, double height, double deflection) {
        if (deflection <= 0 || width <= 0 || height <= 0 || beamSpan <= 0 || load <= 0) {
            Log.w(TAG, "Invalid parameters for stiffness calculation: load=" + load + 
                  ", beamSpan=" + beamSpan + ", width=" + width + 
                  ", height=" + height + ", deflection=" + deflection);
            return 0;
        }

        try {
            // S(t) = (P·L³)/(4·b·h³·δ(t))
            // 单位转换: N/(mm²) -> MPa (1 MPa = 1 N/mm²)
            double numerator = load * Math.pow(beamSpan, 3);
            double denominator = 4 * width * Math.pow(height, 3) * deflection;
            double stiffness = numerator / denominator;
            
            Log.d(TAG, "Stiffness calculation: numerator=" + numerator + 
                  ", denominator=" + denominator + ", stiffness=" + stiffness);
            
            return stiffness;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating stiffness", e);
            return 0;
        }
    }

    /**
     * 计算蠕变速率(m值)
     * 公式: ln(S(t)) = m·ln(t) + C
     * 使用最小二乘法拟合直线来计算m值
     *
     * @param times 时间点数组 (s)
     * @param stiffnesses 对应时间点的弯曲蠕变劲度模量数组 (MPa)
     * @return 蠕变速率(m值)
     */
    public static double calculateCreepRate(double[] times, double[] stiffnesses) {
        if (times == null || stiffnesses == null || times.length != stiffnesses.length || times.length < 2) {
            Log.w(TAG, "Invalid parameters for creep rate calculation: times=" + 
                  (times == null ? "null" : times.length) + ", stiffnesses=" + 
                  (stiffnesses == null ? "null" : stiffnesses.length));
            return 0;
        }

        try {
            int n = times.length;
            double[] lnTimes = new double[n];
            double[] lnStiffnesses = new double[n];
            
            // 计算ln(t)和ln(S(t))
            for (int i = 0; i < n; i++) {
                if (times[i] <= 0 || stiffnesses[i] <= 0) {
                    Log.w(TAG, "Invalid data point at index " + i + ": time=" + times[i] + ", stiffness=" + stiffnesses[i]);
                    continue;
                }
                lnTimes[i] = Math.log(times[i]);
                lnStiffnesses[i] = Math.log(stiffnesses[i]);
                Log.d(TAG, "Data point " + i + ": time=" + times[i] + ", ln(time)=" + lnTimes[i] + 
                      ", stiffness=" + stiffnesses[i] + ", ln(stiffness)=" + lnStiffnesses[i]);
            }
            
            // 计算最小二乘法所需的和
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            int validPoints = 0;
            
            for (int i = 0; i < n; i++) {
                if (times[i] <= 0 || stiffnesses[i] <= 0) {
                    continue;
                }
                sumX += lnTimes[i];
                sumY += lnStiffnesses[i];
                sumXY += lnTimes[i] * lnStiffnesses[i];
                sumX2 += lnTimes[i] * lnTimes[i];
                validPoints++;
            }
            
            if (validPoints < 2) {
                Log.w(TAG, "Not enough valid data points for creep rate calculation: " + validPoints);
                return 0;
            }
            
            // 计算斜率m
            double creepRate = (validPoints * sumXY - sumX * sumY) / (validPoints * sumX2 - sumX * sumX);
            
            Log.d(TAG, "Creep rate calculation: validPoints=" + validPoints + 
                  ", sumX=" + sumX + ", sumY=" + sumY + 
                  ", sumXY=" + sumXY + ", sumX2=" + sumX2 + 
                  ", creepRate=" + creepRate);
            
            return creepRate;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating creep rate", e);
            return 0;
        }
    }
}
