package com.example.labdata.repository;

import com.example.labdata.model.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Long> {
    
    List<ExperimentData> findByExperimentTaskId(Long experimentTaskId);
    
    // 查找未同步的实验数据
    List<ExperimentData> findBySynced(boolean synced);
    
    // 查找客户端ID对应的实验数据
    Optional<ExperimentData> findByClientId(Long clientId);
    
    // 查找最近更新的实验数据
    @Query("SELECT ed FROM ExperimentData ed WHERE ed.updatedAt > :lastSyncTime")
    List<ExperimentData> findModifiedSince(@Param("lastSyncTime") Instant lastSyncTime);
    
    // 查找特定字段和值的实验数据
    @Query("SELECT ed FROM ExperimentData ed JOIN ed.dataValues dv WHERE dv.fieldName = :fieldName AND dv.value = :value")
    List<ExperimentData> findByFieldNameAndValue(@Param("fieldName") String fieldName, @Param("value") String value);
}
