package com.example.labdata.repository;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioAsphalt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixRatioAsphaltRepository extends JpaRepository<MixRatioAsphalt, Long> {
    List<MixRatioAsphalt> findByMixRatio(MixRatio mixRatio);
    void deleteByMixRatio(MixRatio mixRatio);
}
