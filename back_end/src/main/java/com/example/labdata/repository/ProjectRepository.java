package com.example.labdata.repository;

import com.example.labdata.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOrganizationId(Long organizationId);
    Optional<Project> findByClientId(Long clientId);
    Optional<Project> findByNameAndOrganizationId(String name, Long organizationId);
}
