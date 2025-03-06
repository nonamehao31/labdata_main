package com.example.labdata.repository;

import com.example.labdata.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByCompanyId(String companyId);
    List<Device> findByUserId(Long userId);
}
