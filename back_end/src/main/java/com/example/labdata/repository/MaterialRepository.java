package com.example.labdata.repository;

import com.example.labdata.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    Optional<Material> findByName(String name);
    
    List<Material> findByType(String type);
    
    // 查找未同步的材料
    List<Material> findBySynced(boolean synced);
    
    // 根据客户端ID查找材料
    Optional<Material> findByClientId(Long clientId);
    
    // 根据名称和组织ID查找材料
    Optional<Material> findByNameAndOrganizationId(String name, Long organizationId);
    
    // 根据类型和组织ID查找材料
    List<Material> findByTypeAndOrganizationId(String type, Long organizationId);
    
    // 查找客户端ID是否存在
    boolean existsByClientId(Long clientId);
    
    // 查找未关联到任何混合比例的材料
    @Query("SELECT m FROM Material m WHERE m NOT IN (SELECT DISTINCT mr.materials FROM MixRatio mr)")
    List<Material> findMaterialsNotInAnyMixRatio();
    
    // 查找最近使用的材料
    @Query("SELECT DISTINCT m FROM Material m JOIN MixRatio mr ON m MEMBER OF mr.materials WHERE mr.createdAt > :lastUsedTime")
    List<Material> findRecentlyUsedMaterials(@Param("lastUsedTime") java.time.Instant lastUsedTime);
    
    // 查找最近修改的材料
    @Query("SELECT m FROM Material m WHERE m.updatedAt > :lastSyncTime")
    List<Material> findModifiedSince(@Param("lastSyncTime") Instant lastSyncTime);
    
    // 查找特定组织的材料
    List<Material> findByOrganizationId(Long organizationId);
}
