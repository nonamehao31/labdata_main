package com.example.labdata_main.utils;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentTypeInitializer {
    private static final String TAG = "ExperimentTypeInitializer";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void initializeExperimentTypes(Context context) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // 检查是否已经初始化
            if (db.experimentTypeDao().getCount() > 0) {
                return;
            }

            List<ExperimentType> experimentTypes = new ArrayList<>();

            // 添加沥青实验类型
            experimentTypes.add(new ExperimentType("针入度试验", ExperimentType.TYPE_PENETRATION, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("延度试验", ExperimentType.TYPE_DUCTILITY, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("软化点试验", ExperimentType.TYPE_SOFTENING_POINT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("沥青弯曲蠕变劲度试验(弯曲梁流变仪法)", ExperimentType.TYPE_BBR, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("密度试验", ExperimentType.TYPE_DENSITY, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("薄膜烘箱试验", ExperimentType.TYPE_TFOT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("旋转薄膜烘箱试验", ExperimentType.TYPE_RTFOT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("闪点试验", ExperimentType.TYPE_FLASH_POINT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("粘度试验", ExperimentType.TYPE_VISCOSITY, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("动态剪切流变试验", ExperimentType.TYPE_DSR, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("直接拉伸试验", ExperimentType.TYPE_DTT, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("沥青旋转黏度试验(布鲁克菲尔德黏度计法)", ExperimentType.TYPE_BROOKFIELD_VISCOSITY, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("压力老化试验", ExperimentType.TYPE_PAV, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("多重应力蠕变恢复试验", ExperimentType.TYPE_MSCR, ExperimentType.CATEGORY_ASPHALT));
            experimentTypes.add(new ExperimentType("力学延度试验", ExperimentType.TYPE_FORCE_DUCTILITY, ExperimentType.CATEGORY_ASPHALT));
            
            // 添加沥青混合料实验类型
            experimentTypes.add(new ExperimentType("沥青混合料弯曲试验", ExperimentType.TYPE_MIXTURE_BENDING, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("动态模量试验", ExperimentType.TYPE_DYNAMIC_MODULUS, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料直接拉伸循环疲劳测黏弹损伤试验", ExperimentType.TYPE_DIRECT_STRETCHING_FATIGUE, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料四点弯曲疲劳寿命试验", ExperimentType.TYPE_FOUR_POINT_BENDING, ExperimentType.CATEGORY_MIXTURE));
            experimentTypes.add(new ExperimentType("沥青混合料单轴压缩试验(圆柱体法)", ExperimentType.TYPE_SINGLE_AXIS_COMPRESSION, ExperimentType.CATEGORY_MIXTURE));

            try {
                db.experimentTypeDao().insertAll(experimentTypes);
                Log.d(TAG, "Successfully initialized experiment types");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing experiment types", e);
            }
        });
    }
}
