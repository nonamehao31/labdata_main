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
    
    // 查找组织ID对应的材料信息，用于用户登录后获取组织内的数据
    List<Material> findByOrganizationId(Long organizationId);
    
    // 查找指定类型和组织ID对应的材料
    List<Material> findByTypeAndOrganizationId(String type, Long organizationId);
    
    // 检查客户端ID是否已经存在
    boolean existsByClientId(Long clientId);
    
    // 查找指定名称和组织ID对应的材料
    Optional<Material> findByNameAndOrganizationId(String name, Long organizationId);
    
    // 查找最近更新的材料
    @Query("SELECT m FROM Material m WHERE m.updatedAt > :lastSyncTime")
    List<Material> findModifiedSince(@Param("lastSyncTime") java.time.Instant lastSyncTime);
}
