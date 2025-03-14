package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
