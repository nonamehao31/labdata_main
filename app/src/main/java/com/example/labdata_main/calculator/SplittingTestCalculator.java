package com.example.labdata_main.calculator;

/**
 * 沥青混合料劈裂试验计算器
 * 用于计算劈裂抗拉强度、泊松比、破坏拉伸应变、破坏韧度模量和水平变形
 */
public class SplittingTestCalculator {

    /**
     * 计算劈裂抗拉强度 (MPa)
     * @param maxLoad 试验荷载的最大值 (N)
     * @param height 试件高度 (mm)
     * @param diameter 试件直径 (mm)
     * @return 劈裂抗拉强度 (MPa)
     */
    public static double calculateSplittingTensileStrength(double maxLoad, double height, double diameter) {
        // 根据试件直径选择不同的计算公式
        if (diameter >= 98.0 && diameter <= 102.0) {
            // 当试件直径为100mm±2.0mm时，按公式(T 0716-1)计算
            return 0.006287 * maxLoad / height;
        } else if (diameter >= 147.5 && diameter <= 152.5) {
            // 当试件直径为150.0mm±2.5mm时，按公式(T 0716-2)计算
            return 0.00425 * maxLoad / height;
        } else {
            // 对于其他直径，使用通用公式 2P/(π*d*h)
            return 2 * maxLoad / (Math.PI * diameter * height);
        }
    }

    /**
     * 计算泊松比
     * @param verticalDeformation 垂直方向总变形 (mm)
     * @param horizontalDeformation 水平方向总变形 (mm)
     * @return 泊松比
     */
    public static double calculatePoissonRatio(double verticalDeformation, double horizontalDeformation) {
        // 计算垂直变形与水平变形的比值 A = YT/XT
        double deformationRatio = verticalDeformation / horizontalDeformation;
        
        // 泊松比计算公式 μ = (0.1350A-1.7940)/(-0.5A-0.0314)
        return (0.1350 * deformationRatio - 1.7940) / (-0.5 * deformationRatio - 0.0314);
    }

    /**
     * 计算破坏拉伸应变
     * @param horizontalDeformation 水平方向总变形 (mm)
     * @param poissonRatio 泊松比
     * @return 破坏拉伸应变 (με)
     */
    public static double calculateFailureTensileStrain(double horizontalDeformation, double poissonRatio) {
        // 破坏拉伸应变计算公式 εT = XT*(0.0307+0.0936μ)/(1.35+5μ)
        double strain = horizontalDeformation * (0.0307 + 0.0936 * poissonRatio) / (1.35 + 5 * poissonRatio);
        
        // 转换为微应变 (με)
        return strain * 1000000;
    }

    /**
     * 计算破坏韧度模量 (MPa)
     * @param maxLoad 试验荷载的最大值 (N)
     * @param height 试件高度 (mm)
     * @param horizontalDeformation 水平方向总变形 (mm)
     * @param poissonRatio 泊松比
     * @return 破坏韧度模量 (MPa)
     */
    public static double calculateStiffnessModulus(double maxLoad, double height, double horizontalDeformation, double poissonRatio) {
        // 破坏韧度模量计算公式 ST = P(0.27+1.0μ)/(h³XT)
        return maxLoad * (0.27 + 1.0 * poissonRatio) / (Math.pow(height, 3) * horizontalDeformation);
    }

    /**
     * 计算水平变形 (mm)
     * @param verticalDeformation 垂直方向总变形 (mm)
     * @param poissonRatio 泊松比
     * @return 水平方向总变形 (mm)
     */
    public static double calculateHorizontalDeformation(double verticalDeformation, double poissonRatio) {
        // 水平变形计算公式 XT = YT*(0.135+0.5μ)/(1.794-0.0314μ)
        return verticalDeformation * (0.135 + 0.5 * poissonRatio) / (1.794 - 0.0314 * poissonRatio);
    }
    
    /**
     * 根据温度估算泊松比
     * 基于常见的温度-泊松比对应关系，为自动计算提供初始值
     * @param temperature 测试温度 (°C)
     * @return 估算的泊松比
     */
    public static double getPoissonRatioFromTemperature(double temperature) {
        // 根据温度范围返回估算的泊松比值
        if (temperature <= -10) {
            return 0.15;
        } else if (temperature <= 0) {
            return 0.20;
        } else if (temperature <= 10) {
            return 0.25;
        } else if (temperature <= 20) {
            return 0.30;
        } else if (temperature <= 30) {
            return 0.35;
        } else if (temperature <= 40) {
            return 0.40;
        } else {
            return 0.45;
        }
    }
}
