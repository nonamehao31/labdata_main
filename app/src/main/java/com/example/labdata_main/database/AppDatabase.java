package com.example.labdata_main.database;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.labdata_main.dao.DeviceDao;
import com.example.labdata_main.dao.ExperimentDataDao;
import com.example.labdata_main.dao.ExperimentTaskDao;
import com.example.labdata_main.dao.MaterialDao;
import com.example.labdata_main.dao.MixRatioDao;
import com.example.labdata_main.dao.MoldingMethodDao;
import com.example.labdata_main.dao.SpecimenDao;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MixDesign;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.Specimen;

@Database(entities = {
    MixRatio.class, 
    Specimen.class, 
    Material.class, 
    MixDesign.class, 
    MoldingMethod.class,
    ExperimentTask.class,
    ExperimentData.class,  // 添加 ExperimentData 实体
    Device.class  // 添加 Device 实体
}, version = 15)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "labdata_db";
    private static volatile AppDatabase instance;

    public abstract MixRatioDao mixRatioDao();
    public abstract SpecimenDao specimenDao();
    public abstract MaterialDao materialDao();
    public abstract MoldingMethodDao moldingMethodDao();
    public abstract ExperimentTaskDao experimentTaskDao();
    public abstract ExperimentDataDao experimentDataDao();  // 添加 ExperimentDataDao
    public abstract DeviceDao deviceDao();  // 添加 DeviceDao

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 1 to version 2");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 2 to version 3");
            database.execSQL("DROP TABLE IF EXISTS specimens");
            database.execSQL("CREATE TABLE IF NOT EXISTS specimens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "mix_ratio_id INTEGER NOT NULL, " +
                    "mixing_temperature REAL NOT NULL, " +
                    "mixing_speed REAL NOT NULL, " +
                    "compaction_method TEXT DEFAULT '', " +
                    "creation_time INTEGER NOT NULL, " +
                    "cut_shape TEXT, " +
                    "cut_count INTEGER DEFAULT 1, " +
                    "length REAL DEFAULT 0, " +
                    "width REAL DEFAULT 0, " +
                    "height REAL DEFAULT 0, " +
                    "radius REAL DEFAULT 0)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 3 to version 4");
            database.execSQL("DROP TABLE IF EXISTS specimens");
            database.execSQL("CREATE TABLE specimens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "mix_ratio_id INTEGER NOT NULL, " +
                    "mixing_temperature REAL NOT NULL, " +
                    "mixing_speed REAL NOT NULL, " +
                    "compaction_method TEXT NOT NULL DEFAULT '', " +
                    "creation_time INTEGER NOT NULL, " +
                    "cut_shape TEXT, " +
                    "cut_count INTEGER NOT NULL DEFAULT 1, " +
                    "length REAL NOT NULL DEFAULT 0, " +
                    "width REAL NOT NULL DEFAULT 0, " +
                    "height REAL NOT NULL DEFAULT 0, " +
                    "radius REAL NOT NULL DEFAULT 0)");
            database.execSQL("CREATE INDEX index_specimens_mix_ratio_id ON specimens(mix_ratio_id)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 4 to version 5");
            database.execSQL("ALTER TABLE mix_ratios RENAME TO mix_ratios_old");
            database.execSQL("CREATE TABLE mix_ratios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "creation_time INTEGER NOT NULL, " +
                    "materials TEXT NOT NULL)");
            database.execSQL("INSERT INTO mix_ratios (id, name, description, creation_time, materials) " +
                    "SELECT id, name, description, creation_time, materials FROM mix_ratios_old");
            database.execSQL("DROP TABLE mix_ratios_old");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 5 to version 6");
            // 创建材料属性表
            database.execSQL("CREATE TABLE IF NOT EXISTS material_properties (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "unit TEXT)");

            // 创建配合比设计表
            database.execSQL("CREATE TABLE IF NOT EXISTS mix_designs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "designGroup INTEGER NOT NULL, " +
                    "materialType TEXT NOT NULL, " +
                    "materialName TEXT NOT NULL, " +
                    "proportion REAL NOT NULL)");

            // 创建材料表
            database.execSQL("CREATE TABLE IF NOT EXISTS materials (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "category TEXT)");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 6 to version 7");
            database.execSQL("CREATE TABLE IF NOT EXISTS experiment_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "taskId TEXT NOT NULL, " +
                    "projectId INTEGER NOT NULL, " +
                    "projectName TEXT NOT NULL, " +
                    "creationTime INTEGER NOT NULL, " +
                    "selectedMixRatios TEXT NOT NULL, " +
                    "experimentAssignments TEXT NOT NULL, " +
                    "moldingMethod TEXT NOT NULL, " +
                    "notes TEXT)");
            
            database.execSQL("CREATE INDEX index_experiment_tasks_taskId ON experiment_tasks(taskId)");
            database.execSQL("CREATE INDEX index_experiment_tasks_projectId ON experiment_tasks(projectId)");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 7 to version 8");
            
            // 添加新的列到 experiment_tasks 表
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN deadline INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN preparation_time INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN specimen_generation_time INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN experiment_completion_time INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN status TEXT");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 8 to version 9");
            
            // 添加公司ID列到 experiment_tasks 表
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN companyId TEXT");
            
            // 创建索引以加快按公司查询的速度
            database.execSQL("CREATE INDEX IF NOT EXISTS index_experiment_tasks_companyId ON experiment_tasks(companyId)");

            // 创建设备表
            database.execSQL("CREATE TABLE IF NOT EXISTS devices (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +  // MIXING, FORMING, TESTING
                    "manufacturer TEXT NOT NULL, " +
                    "model TEXT NOT NULL, " +
                    "purchase_year TEXT NOT NULL, " +
                    "company_id TEXT NOT NULL)");
            
            // 创建索引以加快按公司查询设备的速度
            database.execSQL("CREATE INDEX IF NOT EXISTS index_devices_company_id ON devices(company_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_devices_type ON devices(type)");
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 9 to version 10");
            database.execSQL("ALTER TABLE molding_methods ADD COLUMN mix_ratio_id INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建experiment_data表
            database.execSQL("CREATE TABLE IF NOT EXISTS `experiment_data` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`taskId` INTEGER NOT NULL, " +
                    "`experimentName` TEXT, " +
                    "`input1Label` TEXT, " +
                    "`input2Label` TEXT, " +
                    "`input1Value` REAL NOT NULL, " +
                    "`input2Value` REAL NOT NULL, " +
                    "`deviceManufacturer` TEXT, " +
                    "`deviceModel` TEXT, " +
                    "`devicePurchaseYear` TEXT, " +
                    "`createTime` INTEGER NOT NULL)");

            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_experimentName` ON `experiment_data` (`experimentName`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_input1` ON `experiment_data` (`experimentName`, `input1Label`, `input1Value`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_input2` ON `experiment_data` (`experimentName`, `input2Label`, `input2Value`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_device` ON `experiment_data` (`deviceManufacturer`, `deviceModel`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_taskId` ON `experiment_data` (`taskId`)");
        }
    };

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加 experimenter 列
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN experimenter TEXT");
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加实验人员字段
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN experimenter TEXT");
            // 添加实验完成时间字段
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN experiment_completion_time INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 13 to version 14");
            database.execSQL("ALTER TABLE experiment_data ADD COLUMN mixRatio TEXT");
            database.execSQL("ALTER TABLE experiment_data ADD COLUMN result TEXT");
        }
    };

    static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加新的索引
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_mixRatio` ON `experiment_data` (`mixRatio`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiment_data_result` ON `experiment_data` (`result`)");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                                    MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                                    MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
                            .build();
                }
            }
        }
        return instance;
    }
}