package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MixRatioRepository extends JpaRepository<MixRatio, Long> {
    
    Optional<MixRatio> findByName(String name);
    
    // 查找未同步的混合比例
    List<MixRatio> findBySynced(boolean synced);
    
    // 查找客户端ID对应的混合比例
    Optional<MixRatio> findByClientId(Long clientId);
    
    // 查找最近更新的混合比例
    @Query("SELECT mr FROM MixRatio mr WHERE mr.updatedAt > :lastSyncTime")
    List<MixRatio> findModifiedSince(@Param("lastSyncTime") java.time.Instant lastSyncTime);
    
    // 查找包含特定材料的混合比例
    @Query("SELECT mr FROM MixRatio mr JOIN mr.materials m WHERE m.id = :materialId")
    List<MixRatio> findByMaterialId(@Param("materialId") Long materialId);
}
