package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.labdata_main.model.MoldingMethod;

import java.util.List;

@Dao
public interface MoldingMethodDao {
    @Insert
    long insert(MoldingMethod moldingMethod);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MoldingMethod... methods);

    @Query("SELECT * FROM molding_methods")
    List<MoldingMethod> getAllMoldingMethods();

    @Delete
    void delete(MoldingMethod moldingMethod);
    
    @Query("DELETE FROM molding_methods")
    void deleteAllMoldingMethods();
}
