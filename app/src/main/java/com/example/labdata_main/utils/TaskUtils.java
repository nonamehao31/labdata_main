package com.example.labdata_main.utils;

import com.example.labdata_main.model.ExperimentTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务相关的工具类
 */
public class TaskUtils {

    // 任务类别常量
    public static final String CATEGORY_MIXTURE = "MIXTURE";
    public static final String CATEGORY_ASPHALT = "ASPHALT";

    /**
     * 按任务类别过滤任务列表
     * @param tasks 原始任务列表
     * @param category 任务类别
     * @return 过滤后的任务列表
     */
    public static List<ExperimentTask> filterTasksByCategory(List<ExperimentTask> tasks, String category) {
        if (tasks == null) {
            return new ArrayList<>();
        }
        
        // 使用Stream API进行过滤
        return tasks.stream()
                .filter(task -> category.equals(task.getExperimentType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取任务列表中的沥青实验任务
     * @param tasks 原始任务列表
     * @return 过滤后的沥青实验任务列表
     */
    public static List<ExperimentTask> getAsphaltTasks(List<ExperimentTask> tasks) {
        return filterTasksByCategory(tasks, CATEGORY_ASPHALT);
    }

    /**
     * 获取任务列表中的混合料实验任务
     * @param tasks 原始任务列表
     * @return 过滤后的混合料实验任务列表
     */
    public static List<ExperimentTask> getMixtureTasks(List<ExperimentTask> tasks) {
        return filterTasksByCategory(tasks, CATEGORY_MIXTURE);
    }
}
