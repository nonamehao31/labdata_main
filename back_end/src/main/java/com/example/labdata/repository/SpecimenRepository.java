package com.example.labdata.repository;

import com.example.labdata.model.Specimen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Long> {
    
    Optional<Specimen> findByCode(String code);
    
    List<Specimen> findByMixRatioId(Long mixRatioId);
    
    // 查找未同步的样品
    List<Specimen> findBySynced(boolean synced);
    
    // 查找客户端ID对应的样品
    Optional<Specimen> findByClientId(Long clientId);
    
    // 查找最近更新的样品
    @Query("SELECT s FROM Specimen s WHERE s.updatedAt > :lastSyncTime")
    List<Specimen> findModifiedSince(@Param("lastSyncTime") java.time.Instant lastSyncTime);
}
