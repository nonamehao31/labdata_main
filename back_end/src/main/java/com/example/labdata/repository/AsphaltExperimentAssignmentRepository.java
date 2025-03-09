package com.example.labdata.repository;

import com.example.labdata.model.AsphaltExperimentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsphaltExperimentAssignmentRepository extends JpaRepository<AsphaltExperimentAssignment, Long> {
    List<AsphaltExperimentAssignment> findByAsphaltInfoId(Long asphaltInfoId);
    List<AsphaltExperimentAssignment> findByExperimentTaskId(Long experimentTaskId);
    List<AsphaltExperimentAssignment> findByClientId(Long clientId);
}
