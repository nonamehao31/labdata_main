// 修复四点弯曲疲劳寿命试验保存方法的正确代码
private void saveFourPointBendingFatigueTestData(String mixRatioId, Map<String, String> experiments) {
    // 创建请求数据
    Map<String, Object> requestData = new HashMap<>();
    
    // 使用沥青混合料四点弯曲疲劳寿命试验对应的任务ID
    String taskId = getTaskIdForExperimentType("沥青混合料四点弯曲疲劳寿命试验");
    requestData.put("taskId", taskId);
    
    requestData.put("mixRatioId", mixRatioId);
    
    // 实验名称和日志记录
    String experimentName = "沥青混合料四点弯曲疲劳寿命试验";
    Log.i(TAG, "正在保存配比 " + mixRatioId + " 的" + experimentName + "数据");
    
    // 基础实验参数
    String testDate = experiments.get(experimentName + "_testDate");
    String operator = experiments.get(experimentName + "_operator");
    String mixTemperature = experiments.get(experimentName + "_mixTemperature");
    String mixSpeed = experiments.get(experimentName + "_mixSpeed");
    String mixTime = experiments.get(experimentName + "_mixTime");
    String compactionMethod = experiments.get(experimentName + "_compactionMethod");
    
    requestData.put("mixRatioName", currentTask.getMixRatioName(mixRatioId));
    requestData.put("experimentName", experimentName);
    requestData.put("mixTemperature", mixTemperature);
    requestData.put("mixSpeed", mixSpeed);
    requestData.put("mixTime", mixTime);
    requestData.put("compactionMethod", compactionMethod);
    requestData.put("testDate", testDate);
    requestData.put("operator", operator);
    
    // 统计有多少个试件
    int specimenCount = 0;
    for (int i = 1; i <= 5; i++) {  // 假设最多5个试件
        if (experiments.containsKey(experimentName + "_width_" + i)) {
            specimenCount = i;
        }
    }
    Log.d(TAG, "找到 " + specimenCount + " 个有效试件");
    
    // 收集所有试件的数据
    List<Map<String, Object>> specimensData = new ArrayList<>();
    
    // 准备结果类型映射表，确保后端能正确保存
    String[][] resultTypeMapping = {
        {"最大拉应力", "Tensile stress", "kPa"},
        {"最大拉应变", "Tensile strain", "με"},
        {"弯曲劲度模量", "Flexural stiffness", "MPa"},
        {"相位角", "Phase Angle", "deg"},
        {"单个循环耗散能", "Dissipated energy", "J/m³"},
        {"累积耗散能", "Cumulative dissipated energy", "kJ/m³"}
    };
    
    for (int i = 1; i <= specimenCount; i++) {
        // 获取试件参数
        Map<String, Object> specimen = new HashMap<>();
        
        // 试件ID
        specimen.put("specimenId", UUID.randomUUID().toString());
        specimen.put("specimenNumber", i);
        
        // 试件尺寸
        Float width = parseFloatSafely(experiments.get(experimentName + "_width_" + i));
        Float height = parseFloatSafely(experiments.get(experimentName + "_height_" + i));
        Float length = parseFloatSafely(experiments.get(experimentName + "_length_" + i));
        Float spanMm = parseFloatSafely(experiments.get(experimentName + "_span_" + i));
        Float strainRange = parseFloatSafely(experiments.get(experimentName + "_strain_range_" + i));
        Float frequencyHz = parseFloatSafely(experiments.get(experimentName + "_frequency_" + i));
        Float testTemperature = parseFloatSafely(experiments.get(experimentName + "_temperature_" + i));
        
        specimen.put("width", width);     // 映射到width_mm
        specimen.put("height", height);   // 映射到height_mm 
        specimen.put("length", length);   // 映射到length_mm
        specimen.put("spanMm", spanMm);   // 映射到span_mm
        specimen.put("strainRange", strainRange);   // 映射到strain_range
        specimen.put("frequencyHz", frequencyHz);   // 映射到frequency_hz
        specimen.put("testTemperature", testTemperature);   // 映射到test_temperature
        
        // 最终疲劳寿命 - 直接对应fatigue_life字段
        String fatigueLifeKey = experimentName + "_result_" + i + "_fatigue_life";
        Float fatigueLife = parseFloatSafely(experiments.get(fatigueLifeKey));
        specimen.put("fatigueLife", fatigueLife);
        
        // 添加试验结果数据
        List<Map<String, Object>> resultsList = new ArrayList<>();
        
        for (int j = 1; j <= 6; j++) {  // 6个结果参数（不包括疲劳寿命）
            String initialKey = experimentName + "_result_" + i + "_initial_" + j;
            String currentKey = experimentName + "_result_" + i + "_current_" + j;
            
            Float initialValue = parseFloatSafely(experiments.get(initialKey));
            Float currentValue = parseFloatSafely(experiments.get(currentKey));
            
            Map<String, Object> result = new HashMap<>();
            result.put("resultType", j);
            result.put("resultTypeDisplayName", resultTypeMapping[j-1][0]);
            result.put("resultTypeEnglishName", resultTypeMapping[j-1][1]);
            result.put("resultTypeUnit", resultTypeMapping[j-1][2]);
            result.put("initialValue", initialValue);
            result.put("currentValue", currentValue);
            
            resultsList.add(result);
            
            Log.d(TAG, "试件" + i + "参数" + j + " 初始值: " + initialValue + ", 实时值: " + currentValue);
        }
        
        specimen.put("results", resultsList);
        specimensData.add(specimen);
        Log.d(TAG, "已添加试件" + i + "数据");
    }
    
    // 记录收集到的试件数量
    Log.d(TAG, "收集到的试件数据数量: " + specimensData.size());
    
    // 将试件数据添加到请求数据中 - 这行是关键，之前缺失
    requestData.put("specimens", specimensData);
    
    // 请求API保存数据
    Log.d(TAG, "准备发送沥青混合料四点弯曲疲劳寿命试验数据到服务器");
    mixtureTaskService.saveFourPointFatigueTestData(requestData)
            .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存成功: " + response.body().getMessage());
                        showToast("沥青混合料四点弯曲疲劳寿命试验数据保存成功");
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                        Log.e(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存失败: " + errorMsg);
                        showToast("沥青混合料四点弯曲疲劳寿命试验数据保存失败: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存请求失败", t);
                    showToast("沥青混合料四点弯曲疲劳寿命试验数据保存请求失败: " + t.getMessage());
                }
            });
}

// 修复直接拉伸循环疲劳试验保存方法中缺失的specimens参数
private void saveDirectStretchingFatigueTestData(String mixRatioId, Map<String, String> experiments) {
    // 基本信息收集
    Log.d(TAG, "开始保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据，配比ID: " + mixRatioId);
    Map<String, Object> requestData = new HashMap<>();
    
    // 使用沥青混合料直接拉伸循环疲劳测黏弹损伤试验对应的任务ID
    String taskId = getTaskIdForExperimentType("沥青混合料直接拉伸循环疲劳测黏弹损伤试验");
    requestData.put("taskId", taskId);
    
    requestData.put("mixRatioId", mixRatioId);
    
    // 1. 测试基本信息 - 映射到direct_stretching_fatigue_test表
    String experimentName = "沥青混合料直接拉伸循环疲劳测黏弹损伤试验";
    String testDate = experiments.getOrDefault(experimentName + "_测试日期", "");
    String operator = experiments.getOrDefault(experimentName + "_操作人员", "");
    String equipmentId = experiments.getOrDefault(experimentName + "_测试设备", "");
    String notes = experiments.getOrDefault(experimentName + "_备注", "");
    
    Map<String, Object> testInfo = new HashMap<>();
    testInfo.put("testDate", testDate);
    testInfo.put("operator", operator);
    testInfo.put("equipmentId", equipmentId);
    testInfo.put("notes", notes);
    
    requestData.put("testInfo", testInfo);
    
    // 2. 确定有多少个试件
    // 假设最多处理5个试件，如果需要更多，修改此处逻辑
    int totalSpecimens = 0;
    for (int i = 1; i <= 5; i++) {
        String diameterKey = experimentName + "_diameter_" + i;
        if (experiments.containsKey(diameterKey) && !experiments.get(diameterKey).isEmpty()) {
            totalSpecimens = i;
        }
    }
    
    Log.d(TAG, "找到 " + totalSpecimens + " 个有效试件");
    
    // 3. 为每个试件收集数据 - 放入specimens列表中
    List<Map<String, Object>> specimensList = new ArrayList<>();
    
    for (int i = 1; i <= totalSpecimens; i++) {
        String specimenId = String.valueOf(i);
        Map<String, Object> specimenData = new HashMap<>();
    
        // 3.1 试件基本信息 - 使用正确的键名格式
        specimenData.put("specimenId", specimenId);
        specimenData.put("height", experiments.getOrDefault(experimentName + "_height_" + specimenId, ""));
        specimenData.put("diameter", experiments.getOrDefault(experimentName + "_diameter_" + specimenId, ""));
        
        // 3.2 动态模量数据 - 使用dynamic前缀
        Map<String, Map<String, String>> modulusData = new HashMap<>();
        
        // 初始动态模量数据
        Map<String, String> initialModulusData = new HashMap<>();
        initialModulusData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_modulus", ""));
        initialModulusData.put("cycleCount", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_cycle", ""));
        initialModulusData.put("phaseAngle", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_phase", ""));
        initialModulusData.put("forceLevel", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_stress", ""));
        initialModulusData.put("uniformStrain", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_strain", ""));
        initialModulusData.put("strainChange", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_actuator", ""));
        initialModulusData.put("temperature", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_temp", ""));
        initialModulusData.put("stage", "initial");
        modulusData.put("initial", initialModulusData);
        
        // 最终动态模量数据
        Map<String, String> finalModulusData = new HashMap<>();
        finalModulusData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_modulus", ""));
        finalModulusData.put("cycleCount", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_cycle", ""));
        finalModulusData.put("phaseAngle", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_phase", ""));
        finalModulusData.put("forceLevel", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_stress", ""));
        finalModulusData.put("uniformStrain", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_strain", ""));
        finalModulusData.put("strainChange", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_actuator", ""));
        finalModulusData.put("temperature", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_temp", ""));
        finalModulusData.put("stage", "final");
        modulusData.put("final", finalModulusData);
        
        specimenData.put("modulusData", modulusData);
        
        // 3.3 疲劳数据 - 使用fatigue前缀
        Map<String, Map<String, String>> fatigueData = new HashMap<>();
        
        // 初始疲劳数据
        Map<String, String> initialFatigueData = new HashMap<>();
        initialFatigueData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_modulus", ""));
        initialFatigueData.put("cycleCount", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_cycle", ""));
        initialFatigueData.put("phaseAngle", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_phase", ""));
        initialFatigueData.put("forceLevel", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_stress", ""));
        initialFatigueData.put("uniformStrain", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_strain", ""));
        initialFatigueData.put("strainChange", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_actuator", ""));
        initialFatigueData.put("temperature", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_temp", ""));
        initialFatigueData.put("stage", "initial");
        fatigueData.put("initial", initialFatigueData);
        
        // 最终疲劳数据
        Map<String, String> finalFatigueData = new HashMap<>();
        finalFatigueData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_modulus", ""));
        finalFatigueData.put("cycleCount", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_cycle", ""));
        finalFatigueData.put("phaseAngle", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_phase", ""));
        finalFatigueData.put("forceLevel", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_stress", ""));
        finalFatigueData.put("uniformStrain", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_strain", ""));
        finalFatigueData.put("strainChange", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_actuator", ""));
        finalFatigueData.put("temperature", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_temp", ""));
        finalFatigueData.put("stage", "final");
        fatigueData.put("final", finalFatigueData);
        
        specimenData.put("fatigueData", fatigueData);
        
        // 添加到试件列表
        specimensList.add(specimenData);
        Log.d(TAG, "添加试件 " + specimenId + " 数据成功");
    }
    
    // 添加试件列表到请求数据 - 这行是关键，之前缺失
    requestData.put("specimens", specimensList);
    
    // 记录完整请求数据
    Log.d(TAG, "准备发送沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据: " + new Gson().toJson(requestData));
    
    // 发送请求
    MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
    Call<ApiResponse<Map<String, String>>> call = apiService.saveDirectStretchingFatigueTestData(requestData);
    
    try {
        Response<ApiResponse<Map<String, String>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            Log.i(TAG, "成功保存配比 " + mixRatioId + " 的" + experimentName + "数据");
            showToast(experimentName + "数据保存成功");
        } else {
            String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
            Log.e(TAG, "保存" + experimentName + "数据失败: " + errorMsg);
            showToast(experimentName + "数据保存失败: " + errorMsg);
            
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "错误详情: " + errorBody);
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "保存" + experimentName + "数据时出错", e);
        showToast(experimentName + "数据保存失败: " + e.getMessage());
    }
}
