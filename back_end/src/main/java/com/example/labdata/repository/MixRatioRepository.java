package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MixRatioRepository extends JpaRepository<MixRatio, Long> {
    
    Optional<MixRatio> findByMixId(String mixId);
    
    @Query("SELECT COUNT(m) FROM MixRatio m WHERE m.createdAt >= ?1 AND m.createdAt < ?2")
    int countMixRatiosCreatedBetween(LocalDateTime start, LocalDateTime end);

    List<MixRatio> findByMixNameContaining(String name);
    
    /**
     * 根据任务ID查询所有相关的配比ID列表（去重）
     * 
     * @param taskId 任务ID
     * @return 配比ID列表
     */
    @Query("SELECT DISTINCT mt.mixratioId FROM UserMixtureTask mt WHERE mt.taskId = :taskId")
    List<Long> findDistinctMixratioIdsByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID查询所有包含特定前缀的任务的相关配比ID列表（去重）
     * 
     * @param taskIdPrefix 任务ID前缀
     * @return 配比ID列表
     */
    @Query("SELECT DISTINCT mt.mixratioId FROM UserMixtureTask mt WHERE mt.taskId LIKE CONCAT(:taskIdPrefix, '%')")
    List<Long> findDistinctMixratioIdsByTaskIdStartingWith(@Param("taskIdPrefix") String taskIdPrefix);
    
    /**
     * 根据公司ID查询该公司的所有配比
     * 
     * @param mixCompany 公司ID
     * @return 该公司的配比列表
     */
    List<MixRatio> findByMixCompany(String mixCompany);
    
    /**
     * 根据字符串类型的公司ID查询该公司的所有配比
     * 
     * @param mixCompany 字符串类型的公司ID
     * @return 该公司的配比列表
     */
    @Query("SELECT m FROM MixRatio m WHERE m.mixCompany = :mixCompany")
    List<MixRatio> findByMixCompanyString(@Param("mixCompany") String mixCompany);
}
