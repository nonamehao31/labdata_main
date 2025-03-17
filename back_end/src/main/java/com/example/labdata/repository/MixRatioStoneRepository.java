package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioStone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioStoneRepository extends JpaRepository<MixRatioStone, Long> {
    List<MixRatioStone> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);
    
    /**
     * 根据配比ID查询石子配比信息
     * 
     * @param mixRatioId 配比ID
     * @return 石子配比信息列表
     */
    @Query("SELECT ms FROM MixRatioStone ms WHERE ms.mixRatio.id = :mixRatioId")
    List<MixRatioStone> findByMixRatioId(@Param("mixRatioId") Long mixRatioId);
}
