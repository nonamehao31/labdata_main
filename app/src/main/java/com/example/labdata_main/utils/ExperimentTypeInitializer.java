package com.example.labdata_main.utils;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExperimentTypeInitializer {
    private static final String TAG = "ExperimentTypeInitializer";

    // 初始化实验类型列表
    public static void initializeExperimentTypes(Context context) {
        // 使用单线程执行器执行数据库操作
        Executor executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            // 获取数据库实例
            AppDatabase db = AppDatabase.getInstance(context);
            
            // 获取已存在的实验类型数量
            int existingCount = db.experimentTypeDao().getCount();
            
            // 如果已有数据，则不再重复初始化
            if (existingCount > 0) {
                Log.d(TAG, "实验类型数据已存在，无需初始化");
                return;
            }
            
            List<ExperimentType> experimentTypes = new ArrayList<>();
            
            // 按照请求，仅保留以下沥青实验类型
            experimentTypes.add(new ExperimentType("针入度试验", ExperimentType.TYPE_PENETRATION, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("延度试验", ExperimentType.TYPE_DUCTILITY, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("软化点试验（环球法）", ExperimentType.TYPE_SOFTENING_POINT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("沥青弯曲蠕变劲度试验（弯曲梁流变仪法）", ExperimentType.TYPE_BBR, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("动态剪切流变试验", ExperimentType.TYPE_DSR, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("沥青旋转黏度试验（布鲁克菲尔德黏度计法）", ExperimentType.TYPE_BROOKFIELD_VISCOSITY, ExperimentType.CATEGORY_ASPHALT));
            
            // 按照请求，仅保留以下沥青混合料实验类型
            // 由于缺少常量定义，我们直接使用字符串定义这些类型
            experimentTypes.add(new ExperimentType("马歇尔稳定度试验", "marshall", ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("动稳定度试验", "dynamic_stability", ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料车辙试验（汉堡车辙）", "hamburg_rutting", ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料弯曲试验", ExperimentType.TYPE_MIXTURE_BENDING, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("动态模量试验", ExperimentType.TYPE_DYNAMIC_MODULUS, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料直接拉伸循环疲劳试验", ExperimentType.TYPE_DIRECT_STRETCHING_FATIGUE, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料四点弯曲疲劳寿命试验", ExperimentType.TYPE_FOUR_POINT_BENDING, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料单轴压缩试验（圆柱体）", ExperimentType.TYPE_SINGLE_AXIS_COMPRESSION, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料劈裂试验", ExperimentType.TYPE_MIX_SPLITTING, ExperimentType.CATEGORY_MIXTURE));
            
            // 将实验类型列表插入数据库
            db.experimentTypeDao().insertAll(experimentTypes);
            
            Log.d(TAG, "实验类型初始化完成");
        });
    }
}
