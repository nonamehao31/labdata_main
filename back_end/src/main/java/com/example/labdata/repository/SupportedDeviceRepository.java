package com.example.labdata.repository;

import com.example.labdata.model.SupportedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportedDeviceRepository extends JpaRepository<SupportedDevice, Long> {
    /**
     * u6839u636eu8bbeu5907u7c7bu578bu67e5u627eu652fu6301u7684u8bbeu5907
     */
    List<SupportedDevice> findByType(String type);
    
    /**
     * u6839u636eu8bbeu5907u7c7bu578bu548cu5382u5546u67e5u627eu652fu6301u7684u8bbeu5907
     */
    List<SupportedDevice> findByTypeAndManufacturer(String type, String manufacturer);
    
    /**
     * u83b7u53d6u6240u6709u652fu6301u7684u5382u5546u5217u8868uff08u6839u636eu8bbeu5907u7c7bu578bu5206u7ec4uff09
     */
    @Query("SELECT DISTINCT d.manufacturer FROM SupportedDevice d WHERE d.type = :type")
    List<String> findDistinctManufacturerByType(@Param("type") String type);
    
    /**
     * u67e5u627eu7279u5b9au7c7bu578bu3001u5382u5546u548cu578bu53f7u7684u8bbeu5907
     */
    SupportedDevice findByTypeAndManufacturerAndModel(String type, String manufacturer, String model);
}
