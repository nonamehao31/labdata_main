package com.example.labdata.repository;

import com.example.labdata.model.MixingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MixingMethodRepository extends JpaRepository<MixingMethod, Long> {
    List<MixingMethod> findByOrganizationId(Long organizationId);
    Optional<MixingMethod> findByNameAndOrganizationId(String name, Long organizationId);
    Optional<MixingMethod> findByClientId(Long clientId);
}
