package com.example.labdata.repository;

import com.example.labdata.model.ExperimentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExperimentTaskRepository extends JpaRepository<ExperimentTask, Long> {
    
    List<ExperimentTask> findByAssignedToId(Long userId);
    
    List<ExperimentTask> findBySpecimenId(Long specimenId);
    
    List<ExperimentTask> findByExperimentTypeId(Long experimentTypeId);
    
    List<ExperimentTask> findByStatus(ExperimentTask.TaskStatus status);
    
    // 查找日期范围内的任务
    List<ExperimentTask> findByScheduledStartTimeBetween(Instant start, Instant end);
    
    // 查找未同步的任务
    List<ExperimentTask> findBySynced(boolean synced);
    
    // 查找客户端ID对应的任务
    Optional<ExperimentTask> findByClientId(Long clientId);
    
    // 查找按项目ID筛选的任务
    List<ExperimentTask> findByProjectId(Long projectId);
    
    // 查找按制件方法ID筛选的任务
    List<ExperimentTask> findByMixingMethodId(Long mixingMethodId);
    
    // 查找按配比ID筛选的任务
    List<ExperimentTask> findByMixRatioId(Long mixRatioId);
    
    // 查找包含特定原料的任务
    @Query("SELECT et FROM ExperimentTask et JOIN et.materials m WHERE m.id = :materialId")
    List<ExperimentTask> findByMaterialId(@Param("materialId") Long materialId);
    
    // 查找按组织ID筛选的任务
    List<ExperimentTask> findByOrganizationId(Long organizationId);
    
    // 查找最近更新的任务
    @Query("SELECT et FROM ExperimentTask et WHERE et.updatedAt > :lastSyncTime")
    List<ExperimentTask> findModifiedSince(@Param("lastSyncTime") Instant lastSyncTime);
    
    // 根据任务类别查找任务
    List<ExperimentTask> findByCategory(ExperimentTask.ExperimentCategory category);
}
