package com.example.labdata_main.api.service;

import com.example.labdata_main.api.ApiClient;

/**
 * API服务创建工具类
 */
public class ServiceCreator {
    
    /**
     * 创建实验任务服务
     * @return ExperimentTaskService实例
     */
    public static ExperimentTaskService createExperimentTaskService() {
        return ApiClient.getClient().create(ExperimentTaskService.class);
    }
    
    /**
     * 创建项目服务
     * @return ProjectService实例
     */
    public static ProjectService createProjectService() {
        return ApiClient.getClient().create(ProjectService.class);
    }
    
    /**
     * 创建试件服务
     * @return SpecimenService实例
     */
    public static SpecimenService createSpecimenService() {
        return ApiClient.getClient().create(SpecimenService.class);
    }
    
    /**
     * 创建混合料任务服务
     * @return MixtureTaskService实例
     */
    public static MixtureTaskService createMixtureTaskService() {
        return ApiClient.getClient().create(MixtureTaskService.class);
    }
    
    /**
     * 通用服务创建方法
     * @param serviceClass 服务类
     * @param <T> 服务类泛型
     * @return 服务实例
     */
    public static <T> T create(Class<T> serviceClass) {
        return ApiClient.getClient().create(serviceClass);
    }
}
