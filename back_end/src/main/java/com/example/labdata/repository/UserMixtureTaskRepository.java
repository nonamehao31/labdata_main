package com.example.labdata.repository;

import com.example.labdata.model.UserMixtureTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户混合料任务数据访问接口
 */
@Repository
public interface UserMixtureTaskRepository extends JpaRepository<UserMixtureTask, Long> {
    
    /**
     * 根据任务ID查找所有关联任务
     * @param taskId 任务ID
     * @return 关联任务列表
     */
    List<UserMixtureTask> findByTaskId(String taskId);
    
    /**
     * 根据用户ID查找所有任务
     * @param estBy 用户ID
     * @return 用户任务列表
     */
    List<UserMixtureTask> findByEstBy(Long estBy);
    
    /**
     * 根据单位ID查找所有任务
     * @param taskCompany 单位ID
     * @return 单位任务列表
     */
    List<UserMixtureTask> findByTaskCompany(Long taskCompany);
    
    /**
     * 根据项目ID查找所有任务
     * @param projectId 项目ID
     * @return 项目任务列表
     */
    List<UserMixtureTask> findByProjectId(Long projectId);
    
    /**
     * 根据配比ID查找所有任务
     * @param mixratioId 配比ID
     * @return 配比任务列表
     */
    List<UserMixtureTask> findByMixratioId(Long mixratioId);
}
