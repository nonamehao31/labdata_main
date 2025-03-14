package com.example.labdata.repository;

import com.example.labdata.model.AsphaltMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsphaltMaterialRepository extends JpaRepository<AsphaltMaterial, Long> {
    List<AsphaltMaterial> findByCompany(String company);
}
