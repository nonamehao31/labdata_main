package com.example.labdata.repository;

import com.example.labdata.model.ExperimentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExperimentTypeRepository extends JpaRepository<ExperimentType, Long> {
    
    Optional<ExperimentType> findByName(String name);
    
    // 查找未同步的实验类型
    List<ExperimentType> findBySynced(boolean synced);
    
    // 查找客户端ID对应的实验类型
    Optional<ExperimentType> findByClientId(Long clientId);
    
    // 查找最近更新的实验类型
    @Query("SELECT et FROM ExperimentType et WHERE et.updatedAt > :lastSyncTime")
    List<ExperimentType> findModifiedSince(@Param("lastSyncTime") Instant lastSyncTime);
}
