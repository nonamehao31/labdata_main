package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioSand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioSandRepository extends JpaRepository<MixRatioSand, Long> {
    List<MixRatioSand> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);
    
    /**
     * 根据配比ID查询沙子配比信息
     * 
     * @param mixRatioId 配比ID
     * @return 沙子配比信息列表
     */
    @Query("SELECT ms FROM MixRatioSand ms WHERE ms.mixRatio.id = :mixRatioId")
    List<MixRatioSand> findByMixRatioId(@Param("mixRatioId") Long mixRatioId);
}
