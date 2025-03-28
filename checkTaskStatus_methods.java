/**
 * 检查任务状态，如果已完成则显示提示并退出
 * @param taskId 任务ID
 */
private void checkTaskStatus(String taskId) {
    showLoading(true, "检查任务状态...");
    
    // 使用新的API端点获取各个实验类型的状态
    mixtureTaskService.getExperimentTypeStatus(taskId)
        .enqueue(new Callback<ApiResponse<List<Map<String, String>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, String>>>> call, Response<ApiResponse<List<Map<String, String>>>> response) {
                // 添加详细日志帮助调试
                Log.d(TAG, "实验类型状态检查响应: " + (response.isSuccessful() ? "成功" : "失败") + 
                      ", 状态码: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, String>> taskStatusList = response.body().getData();
                    Log.d(TAG, "任务 " + taskId + " 的实验类型状态列表: " + taskStatusList);
                    
                    if (taskStatusList == null || taskStatusList.isEmpty()) {
                        Log.w(TAG, "未找到任务状态数据");
                        showLoading(false);
                        loadTaskData(taskId, true);
                        return;
                    }
                    
                    // 提取任务ID前缀（如果任务ID包含连字符）
                    String taskIdPrefix = taskId;
                    int dashIndex = taskId.indexOf('-');
                    if (dashIndex > 0) {
                        taskIdPrefix = taskId.substring(0, dashIndex);
                    }
                    
                    // 跟踪所有相关任务的状态
                    boolean allPrepareFinished = true;
                    boolean allMakingFinished = true;
                    boolean allTestingFinished = true;
                    
                    // 检查每个任务的状态
                    for (Map<String, String> taskStatus : taskStatusList) {
                        // 提取状态
                        String currentTaskId = taskStatus.getOrDefault("taskId", "");
                        String prepareStatus = taskStatus.getOrDefault("prepareStatus", "unfinished");
                        String makingStatus = taskStatus.getOrDefault("makingStatus", "unfinished");
                        String testingStatus = taskStatus.getOrDefault("testingStatus", "unfinished");
                        
                        Log.d(TAG, "检查任务 ID: " + currentTaskId + 
                              ", 准备状态: " + prepareStatus + 
                              ", 制件状态: " + makingStatus + 
                              ", 测试状态: " + testingStatus);
                        
                        // 检查各个状态是否完成
                        if (!"finished".equalsIgnoreCase(prepareStatus)) {
                            allPrepareFinished = false;
                        }
                        if (!"finished".equalsIgnoreCase(makingStatus)) {
                            allMakingFinished = false;
                        }
                        if (!"finished".equalsIgnoreCase(testingStatus)) {
                            allTestingFinished = false;
                        }
                    }
                    
                    // 只有当所有任务的三个状态都是finished时，才认为任务组已完成
                    boolean allFinished = allPrepareFinished && allMakingFinished && allTestingFinished;
                    
                    Log.d(TAG, "任务组前缀: " + taskIdPrefix + 
                          ", 所有准备完成: " + allPrepareFinished + 
                          ", 所有制件完成: " + allMakingFinished + 
                          ", 所有测试完成: " + allTestingFinished + 
                          ", 总体完成: " + allFinished);
                    
                    if (allFinished) {
                        // 所有状态都已完成，显示提示并退出
                        showLoading(false);
                        new AlertDialog.Builder(RecordMixtureExperimentDataActivity.this)
                            .setTitle("任务已完成")
                            .setMessage("该实验任务组的所有实验(准备、制件和测试)都已标记为完成，无需再次记录数据。")
                            .setPositiveButton("确定", (dialog, which) -> {
                                finish(); // 关闭当前Activity
                            })
                            .setCancelable(false)
                            .show();
                    } else {
                        // 至少有一个状态未完成，继续加载任务数据
                        showLoading(false);
                        loadTaskData(taskId, true);
                    }
                } else {
                    // API响应失败，添加更详细的日志
                    String errorMessage = "获取实验类型状态失败: ";
                    if (response.body() != null) {
                        errorMessage += response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMessage += response.errorBody().string();
                        } catch (IOException e) {
                            errorMessage += "无法读取错误信息";
                        }
                    } else {
                        errorMessage += "未知错误，状态码: " + response.code();
                    }
                    Log.e(TAG, errorMessage);
                    
                    // 请求失败，降级为使用旧方法检查状态
                    fallbackToOldStatusCheck(taskId);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, String>>>> call, Throwable t) {
                Log.e(TAG, "获取实验类型状态请求失败", t);
                // 请求失败，降级为使用旧方法检查状态
                fallbackToOldStatusCheck(taskId);
            }
        });
}

/**
 * 当新API请求失败时，降级使用旧的状态检查方法
 * @param taskId 任务ID
 */
private void fallbackToOldStatusCheck(String taskId) {
    Log.d(TAG, "降级为使用旧的状态检查方法");
    
    mixtureTaskService.getTestingStatus(taskId)
        .enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String status = response.body().getData();
                    Log.d(TAG, "任务 " + taskId + " 的测试状态: " + status);
                    
                    if ("finished".equalsIgnoreCase(status)) {
                        // 任务已完成，显示提示并退出
                        showLoading(false);
                        new AlertDialog.Builder(RecordMixtureExperimentDataActivity.this)
                            .setTitle("任务已完成")
                            .setMessage("该实验任务已标记为完成，无需再次记录数据。")
                            .setPositiveButton("确定", (dialog, which) -> {
                                finish(); // 关闭当前Activity
                            })
                            .setCancelable(false)
                            .show();
                    } else {
                        // 任务未完成，继续加载任务数据
                        showLoading(false);
                        loadTaskData(taskId, true);
                    }
                } else {
                    // API响应失败，仍然加载任务数据
                    showLoading(false);
                    loadTaskData(taskId, true);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "获取任务状态请求失败", t);
                // 请求失败，仍然加载任务数据
                showLoading(false);
                loadTaskData(taskId, true);
            }
        });
}
