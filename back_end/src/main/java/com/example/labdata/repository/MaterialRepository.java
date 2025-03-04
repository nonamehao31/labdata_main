package com.example.labdata.repository;

import com.example.labdata.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    Optional<Material> findByName(String name);
    
    List<Material> findByType(String type);
    
    // 查找未同步的材料
    List<Material> findBySynced(boolean synced);
    
    // 查找客户端ID对应的材料
    Optional<Material> findByClientId(Long clientId);
    
    // 查找最近更新的材料
    @Query("SELECT m FROM Material m WHERE m.updatedAt > :lastSyncTime")
    List<Material> findModifiedSince(@Param("lastSyncTime") java.time.Instant lastSyncTime);
}
