package com.example.labdata_main.database;

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
    long insert(MoldingMethod method);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MoldingMethod... methods);

    @Delete
    void delete(MoldingMethod method);

    @Query("DELETE FROM molding_methods")
    void deleteAllMoldingMethods();

    @Query("SELECT * FROM molding_methods")
    List<MoldingMethod> getAllMoldingMethods();
}
