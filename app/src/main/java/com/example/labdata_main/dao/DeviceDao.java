package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.Device;

import java.util.List;

@Dao
public interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Device device);

    @Update
    void update(Device device);

    @Delete
    void delete(Device device);

    @Query("SELECT * FROM devices WHERE company_id = :companyId")
    List<Device> getDevicesByCompanyId(String companyId);

    @Query("DELETE FROM devices WHERE company_id = :companyId")
    void deleteDevicesByCompanyId(String companyId);

    @Query("SELECT * FROM devices WHERE id = :deviceCode")
    Device getDeviceByCode(String deviceCode);

    @Query("SELECT * FROM devices WHERE type = :type AND manufacturer = :manufacturer " +
           "AND model = :model AND purchase_year = :purchaseYear LIMIT 1")
    Device findMatchingDevice(String type, String manufacturer, String model, String purchaseYear);
}
