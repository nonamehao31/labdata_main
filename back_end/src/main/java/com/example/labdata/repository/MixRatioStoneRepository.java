package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioStone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioStoneRepository extends JpaRepository<MixRatioStone, Long> {
    List<MixRatioStone> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);
}
