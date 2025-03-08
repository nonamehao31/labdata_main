package com.example.labdata.repository;

import com.example.labdata.model.MixingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixingMethodRepository extends JpaRepository<MixingMethod, Long> {
    List<MixingMethod> findByOrganizationId(Long organizationId);
    MixingMethod findByClientId(Long clientId);
}
