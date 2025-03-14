package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioSand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioSandRepository extends JpaRepository<MixRatioSand, Long> {
    List<MixRatioSand> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);
}
