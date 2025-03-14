package com.example.labdata.repository;

import com.example.labdata.model.SandMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SandMaterialRepository extends JpaRepository<SandMaterial, Long> {
    List<SandMaterial> findByCompany(String company);
}
