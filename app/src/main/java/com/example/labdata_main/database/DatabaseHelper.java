package com.example.labdata_main.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.labdata_main.dao.ProjectDao;
import com.example.labdata_main.dao.MixRatioDao;
import com.example.labdata_main.dao.MaterialDao;
import com.example.labdata_main.dao.MaterialPropertyDao;
import com.example.labdata_main.dao.MixDesignDao;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.model.MixDesign;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Database(entities = {Project.class, MixRatio.class, Material.class, MaterialProperty.class, MixDesign.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class DatabaseHelper extends RoomDatabase {
    private static final String DATABASE_NAME = "labdata.db";
    private static DatabaseHelper instance;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract ProjectDao projectDao();
    public abstract MixRatioDao mixRatioDao();
    public abstract MaterialDao materialDao();
    public abstract MaterialPropertyDao materialPropertyDao();
    public abstract MixDesignDao mixDesignDao();

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    DatabaseHelper.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public List<Project> getAllProjects() {
        return projectDao().getAllProjects();
    }

    public List<MixRatio> getAllMixRatios() {
        return mixRatioDao().getAllMixRatios();
    }

    public List<Material> getAllMaterials() {
        return materialDao().getAllMaterials();
    }

    public List<MaterialProperty> getAllMaterialProperties() {
        return materialPropertyDao().getAllMaterialProperties();
    }

    public List<MixDesign> getAllMixDesigns() {
        return mixDesignDao().getAllMixDesigns();
    }

    public long insertMixRatio(MixRatio mixRatio) {
        try {
            Future<Long> future = databaseWriteExecutor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return mixRatioDao().insert(mixRatio);
                }
            });
            return future.get(); // 等待并返回插入的ID
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void insertProject(Project project) {
        databaseWriteExecutor.execute(() -> projectDao().insert(project));
    }

    public void insertMaterial(Material material) {
        databaseWriteExecutor.execute(() -> materialDao().insert(material));
    }

    public void insertMixDesign(MixDesign mixDesign) {
        databaseWriteExecutor.execute(() -> mixDesignDao().insert(mixDesign));
    }

    public List<Material> getMaterialsByCategory(String category) {
        return materialDao().getMaterialsByCategory(category);
    }

    public List<MaterialProperty> getMaterialPropertiesByType(String type) {
        return materialPropertyDao().getPropertiesByType(type);
    }

    public void insertMaterialProperty(MaterialProperty property) {
        materialPropertyDao().insert(property);
    }

    public void deleteMaterialPropertiesByType(String type) {
        materialPropertyDao().deletePropertiesByType(type);
    }
}
