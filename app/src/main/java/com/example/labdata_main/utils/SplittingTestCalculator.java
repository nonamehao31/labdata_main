package com.example.labdata_main.utils;

import android.util.Log;

/**
 * 沥青混合料劈裂试验计算工具类
 */
public class SplittingTestCalculator {
    private static final String TAG = "SplittingTestCalculator";

    /**
     * 计算劈裂抗拉强度
     * 公式: RT = 0.006287PT / h (T 0716-1) 当试件直径为100mm±2.0mm时
     * 公式: RT = 0.00425PT / h (T 0716-2) 当试件直径为150.0mm±2.5mm时
     *
     * @param load 试验荷载的最大值 (N)
     * @param height 试件高度 (mm)
     * @param diameter 试件直径 (mm)
     * @return 劈裂抗拉强度 (MPa)
     */
    public static double calculateSplittingTensileStrength(double load, double height, double diameter) {
        if (load <= 0 || height <= 0 || diameter <= 0) {
            Log.w(TAG, "Invalid parameters for splitting tensile strength calculation: load=" + load + 
                  ", height=" + height + ", diameter=" + diameter);
            return 0;
        }

        try {
            double coefficient;
            // 根据直径选择公式
            if (diameter >= 98.0 && diameter <= 102.0) {
                // 使用公式 T 0716-1，直径约为100mm
                coefficient = 0.006287;
            } else if (diameter >= 147.5 && diameter <= 152.5) {
                // 使用公式 T 0716-2，直径约为150mm
                coefficient = 0.00425;
            } else {
                // 默认使用公式 T 0716-1
                coefficient = 0.006287;
                Log.w(TAG, "Diameter " + diameter + " is outside standard range, using default formula T 0716-1");
            }

            // RT = coefficient * PT / h
            double strength = coefficient * load / height;
            
            Log.d(TAG, "Splitting tensile strength calculation: coefficient=" + coefficient + 
                  ", load=" + load + ", height=" + height + ", strength=" + strength);
            
            return strength;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating splitting tensile strength", e);
            return 0;
        }
    }

    /**
     * 根据测试温度确定泊松比
     * 表 T 0716 泊松比参考值
     *
     * @param temperature 试验温度 (°C)
     * @return 泊松比 μ
     */
    public static double getPoissonRatioFromTemperature(double temperature) {
        try {
            // 根据表 T 0716 确定泊松比
            if (temperature <= 10) {
                return 0.25;
            } else if (temperature <= 15) {
                return 0.30;
            } else if (temperature <= 20) {
                return 0.35;
            } else if (temperature <= 25) {
                return 0.40;
            } else {
                return 0.45;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error determining Poisson's ratio from temperature", e);
            return 0.35; // 默认值
        }
    }

    /**
     * 计算泊松比
     * 公式: μ = (0.1350A-1.7940)/(-0.5A-0.0314) (T 0716-3)
     *
     * @param verticalDeformation 垂直变形 YT (mm)
     * @param horizontalDeformation 水平变形 XT (mm)
     * @return 泊松比 μ
     */
    public static double calculatePoissonRatio(double verticalDeformation, double horizontalDeformation) {
        if (horizontalDeformation <= 0 || verticalDeformation <= 0) {
            Log.w(TAG, "Invalid parameters for Poisson's ratio calculation: verticalDeformation=" + verticalDeformation + 
                  ", horizontalDeformation=" + horizontalDeformation);
            return 0;
        }

        try {
            // A = YT/XT
            double a = verticalDeformation / horizontalDeformation;
            
            // μ = (0.1350A-1.7940)/(-0.5A-0.0314)
            double numerator = 0.1350 * a - 1.7940;
            double denominator = -0.5 * a - 0.0314;
            
            if (denominator == 0) {
                Log.w(TAG, "Denominator is zero in Poisson's ratio calculation");
                return 0;
            }
            
            double poissonRatio = numerator / denominator;
            
            Log.d(TAG, "Poisson's ratio calculation: A=" + a + 
                  ", numerator=" + numerator + ", denominator=" + denominator + 
                  ", poissonRatio=" + poissonRatio);
            
            return poissonRatio;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating Poisson's ratio", e);
            return 0;
        }
    }

    /**
     * 计算水平变形
     * 公式: XT = YT*(0.135+0.5μ)/(1.794-0.0314μ) (T 0716-6)
     *
     * @param verticalDeformation 垂直变形 YT (mm)
     * @param poissonRatio 泊松比 μ
     * @return 水平变形 XT (mm)
     */
    public static double calculateHorizontalDeformation(double verticalDeformation, double poissonRatio) {
        if (verticalDeformation <= 0 || poissonRatio < 0) {
            Log.w(TAG, "Invalid parameters for horizontal deformation calculation: verticalDeformation=" + verticalDeformation + 
                  ", poissonRatio=" + poissonRatio);
            return 0;
        }

        try {
            // XT = YT*(0.135+0.5μ)/(1.794-0.0314μ)
            double numerator = verticalDeformation * (0.135 + 0.5 * poissonRatio);
            double denominator = 1.794 - 0.0314 * poissonRatio;
            
            if (denominator == 0) {
                Log.w(TAG, "Denominator is zero in horizontal deformation calculation");
                return 0;
            }
            
            double horizontalDeformation = numerator / denominator;
            
            Log.d(TAG, "Horizontal deformation calculation: verticalDeformation=" + verticalDeformation + 
                  ", poissonRatio=" + poissonRatio + ", numerator=" + numerator + 
                  ", denominator=" + denominator + ", horizontalDeformation=" + horizontalDeformation);
            
            return horizontalDeformation;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating horizontal deformation", e);
            return 0;
        }
    }

    /**
     * 计算破坏拉伸应变
     * 公式: εT = XT*(0.0307+0.0936μ)/(1.35+5μ) (T 0716-4)
     *
     * @param horizontalDeformation 水平变形 XT (mm)
     * @param poissonRatio 泊松比 μ
     * @return 破坏拉伸应变 εT
     */
    public static double calculateFailureTensileStrain(double horizontalDeformation, double poissonRatio) {
        if (horizontalDeformation <= 0 || poissonRatio < 0) {
            Log.w(TAG, "Invalid parameters for failure tensile strain calculation: horizontalDeformation=" + horizontalDeformation + 
                  ", poissonRatio=" + poissonRatio);
            return 0;
        }

        try {
            // εT = XT*(0.0307+0.0936μ)/(1.35+5μ)
            double numerator = horizontalDeformation * (0.0307 + 0.0936 * poissonRatio);
            double denominator = 1.35 + 5 * poissonRatio;
            
            if (denominator == 0) {
                Log.w(TAG, "Denominator is zero in failure tensile strain calculation");
                return 0;
            }
            
            double strain = numerator / denominator;
            
            Log.d(TAG, "Failure tensile strain calculation: horizontalDeformation=" + horizontalDeformation + 
                  ", poissonRatio=" + poissonRatio + ", numerator=" + numerator + 
                  ", denominator=" + denominator + ", strain=" + strain);
            
            return strain;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating failure tensile strain", e);
            return 0;
        }
    }

    /**
     * 计算破坏韧度模量
     * 公式: ST = PT³(0.27+1.0μ)/(h³XT) (T 0716-5)
     *
     * @param load 试验荷载的最大值 (N)
     * @param height 试件高度 (mm)
     * @param horizontalDeformation 水平变形 XT (mm)
     * @param poissonRatio 泊松比 μ
     * @return 破坏韧度模量 ST (MPa)
     */
    public static double calculateFailureStiffnessModulus(double load, double height, double horizontalDeformation, double poissonRatio) {
        if (load <= 0 || height <= 0 || horizontalDeformation <= 0 || poissonRatio < 0) {
            Log.w(TAG, "Invalid parameters for failure stiffness modulus calculation: load=" + load + 
                  ", height=" + height + ", horizontalDeformation=" + horizontalDeformation + 
                  ", poissonRatio=" + poissonRatio);
            return 0;
        }

        try {
            // ST = PT³(0.27+1.0μ)/(h³XT)
            double numerator = Math.pow(load, 3) * (0.27 + 1.0 * poissonRatio);
            double denominator = Math.pow(height, 3) * horizontalDeformation;
            
            if (denominator == 0) {
                Log.w(TAG, "Denominator is zero in failure stiffness modulus calculation");
                return 0;
            }
            
            double stiffnessModulus = numerator / denominator;
            
            Log.d(TAG, "Failure stiffness modulus calculation: load=" + load + 
                  ", height=" + height + ", horizontalDeformation=" + horizontalDeformation + 
                  ", poissonRatio=" + poissonRatio + ", numerator=" + numerator + 
                  ", denominator=" + denominator + ", stiffnessModulus=" + stiffnessModulus);
            
            return stiffnessModulus;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating failure stiffness modulus", e);
            return 0;
        }
    }
}
