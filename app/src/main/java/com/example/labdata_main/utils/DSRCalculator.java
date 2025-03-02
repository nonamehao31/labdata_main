package com.example.labdata_main.utils;

import android.util.Log;

/**
 * 动态剪切流变试验计算工具类
 */
public class DSRCalculator {
    private static final String TAG = "DSRCalculator";

    /**
     * 计算复合剪切模量
     * 公式: G* = τmax / γmax
     *
     * @param maxShearStress 最大剪切应力 (Pa)
     * @param maxShearStrain 最大剪切应变 (小数形式，非百分比)
     * @return 复合剪切模量 (Pa)
     */
    public static double calculateComplexModulus(double maxShearStress, double maxShearStrain) {
        if (maxShearStrain <= 0 || maxShearStress <= 0) {
            Log.w(TAG, "Invalid parameters for complex modulus calculation: maxShearStress=" + maxShearStress + 
                  ", maxShearStrain=" + maxShearStrain);
            return 0;
        }

        try {
            // G* = τmax / γmax
            double complexModulus = maxShearStress / maxShearStrain;
            
            Log.d(TAG, "Complex modulus calculation: maxShearStress=" + maxShearStress + 
                  ", maxShearStrain=" + maxShearStrain + ", complexModulus=" + complexModulus);
            
            return complexModulus;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating complex modulus", e);
            return 0;
        }
    }

    /**
     * 计算剪切应力
     * 公式: τ = 2M / (πR³)
     *
     * @param torque 扭矩 (N·m)
     * @param plateRadius 平板半径 (m)
     * @return 剪切应力 (Pa)
     */
    public static double calculateShearStress(double torque, double plateRadius) {
        if (plateRadius <= 0 || torque <= 0) {
            Log.w(TAG, "Invalid parameters for shear stress calculation: torque=" + torque + 
                  ", plateRadius=" + plateRadius);
            return 0;
        }

        try {
            // τ = 2M / (πR³)
            double shearStress = 2 * torque / (Math.PI * Math.pow(plateRadius, 3));
            
            Log.d(TAG, "Shear stress calculation: torque=" + torque + 
                  ", plateRadius=" + plateRadius + ", shearStress=" + shearStress);
            
            return shearStress;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating shear stress", e);
            return 0;
        }
    }

    /**
     * 计算剪切应变
     * 公式: γ = θR / h
     *
     * @param angle 角度 (rad)
     * @param plateRadius 平板半径 (m)
     * @param plateGap 平板间距 (m)
     * @return 剪切应变 (无量纲)
     */
    public static double calculateShearStrain(double angle, double plateRadius, double plateGap) {
        if (plateGap <= 0 || plateRadius <= 0) {
            Log.w(TAG, "Invalid parameters for shear strain calculation: angle=" + angle + 
                  ", plateRadius=" + plateRadius + ", plateGap=" + plateGap);
            return 0;
        }

        try {
            // γ = θR / h
            double shearStrain = angle * plateRadius / plateGap;
            
            Log.d(TAG, "Shear strain calculation: angle=" + angle + 
                  ", plateRadius=" + plateRadius + ", plateGap=" + plateGap + 
                  ", shearStrain=" + shearStrain);
            
            return shearStrain;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating shear strain", e);
            return 0;
        }
    }
}
