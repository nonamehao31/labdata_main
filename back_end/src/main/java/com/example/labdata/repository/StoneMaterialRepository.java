package com.example.labdata.repository;

import com.example.labdata.model.StoneMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoneMaterialRepository extends JpaRepository<StoneMaterial, Long> {
    List<StoneMaterial> findByCompany(String company);
}
