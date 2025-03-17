package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioAsphalt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioAsphaltRepository extends JpaRepository<MixRatioAsphalt, Long> {
    List<MixRatioAsphalt> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);

    /**
     * 根据配比ID查询沥青配比信息
     * 
     * @param mixRatioId 配比ID
     * @return 沥青配比信息列表
     */
    @Query("SELECT ma FROM MixRatioAsphalt ma WHERE ma.mixRatio.id = :mixRatioId")
    List<MixRatioAsphalt> findByMixRatioId(@Param("mixRatioId") Long mixRatioId);
}
