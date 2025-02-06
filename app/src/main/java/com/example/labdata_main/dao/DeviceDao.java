package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.labdata_main.model.Device;
import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM devices WHERE type = :type AND manufacturer = :manufacturer " +
           "AND model = :model AND purchase_year = :purchaseYear LIMIT 1")
    Device findMatchingDevice(String type, String manufacturer, String model, String purchaseYear);

    @Insert
    void insert(Device device);

    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("SELECT * FROM devices WHERE company_id = :companyId")
    List<Device> getDevicesByCompanyId(String companyId);
}
