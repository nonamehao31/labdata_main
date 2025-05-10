package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.labdata_main.model.Specimen;
import java.util.List;

@Dao
public interface SpecimenDao {
    @Query("SELECT * FROM specimens WHERE mix_ratio_id = :mixRatioId ORDER BY creation_time DESC")
    List<Specimen> getSpecimensForMixRatio(long mixRatioId);

    @Query("SELECT * FROM specimens WHERE id = :id")
    Specimen getSpecimenById(long id);

    @Insert
    long insert(Specimen specimen);

    @Update
    void update(Specimen specimen);

    @Delete
    void delete(Specimen specimen);
}
