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
import com.example.labdata_main.dao.AsphaltExperimentDataDao;
import com.example.labdata_main.dao.DeviceDao;
import com.example.labdata_main.dao.ExperimentDataDao;
import com.example.labdata_main.dao.ExperimentDataFieldDao;
import com.example.labdata_main.dao.ExperimentTaskDao;
import com.example.labdata_main.dao.ExperimentTypeDao;
import com.example.labdata_main.dao.MaterialDao;
import com.example.labdata_main.dao.MixRatioDao;
import com.example.labdata_main.dao.MoldingMethodDao;
import com.example.labdata_main.dao.SpecimenDao;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentDataField;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MixDesign;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.Specimen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Database(entities = {
    MixRatio.class, 
    Specimen.class, 
    Material.class, 
    MixDesign.class, 
    MoldingMethod.class,
    ExperimentTask.class,
    ExperimentData.class,  
    Device.class,  
    ExperimentType.class,  
    ExperimentDataField.class,  
    AsphaltExperimentData.class
}, version = 20)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "labdata_db";
    private static volatile AppDatabase INSTANCE;

    public abstract MixRatioDao mixRatioDao();
    public abstract SpecimenDao specimenDao();
    public abstract MaterialDao materialDao();
    public abstract MoldingMethodDao moldingMethodDao();
    public abstract ExperimentTaskDao experimentTaskDao();
    public abstract ExperimentDataDao experimentDataDao();  
    public abstract DeviceDao deviceDao();  
    public abstract ExperimentTypeDao experimentTypeDao();  
    public abstract ExperimentDataFieldDao experimentDataFieldDao();
    public abstract AsphaltExperimentDataDao asphaltExperimentDataDao();

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

    static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 15 to version 16");
            database.execSQL("ALTER TABLE experiment_tasks ADD COLUMN experimentType TEXT");
            database.execSQL("UPDATE experiment_tasks SET experimentType = 'MIXTURE' WHERE experimentType IS NULL");
        }
    };

    static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 16 to version 17");
            database.execSQL("CREATE TABLE IF NOT EXISTS experiment_types (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "category TEXT NOT NULL)");
        }
    };

    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建沥青实验数据表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `asphalt_experiment_data` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`taskId` INTEGER NOT NULL, " +
                "`experimentType` TEXT, " +
                "`deviceCode` TEXT, " +
                "`createTime` INTEGER NOT NULL, " +
                "`experimenter` TEXT, " +
                "`experimentValues` TEXT)"
            );

            // 创建索引以提高查询性能
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_asphalt_experiment_data_taskId` " +
                "ON `asphalt_experiment_data` (`taskId`)"
            );

            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_asphalt_experiment_data_experimentType` " +
                "ON `asphalt_experiment_data` (`experimentType`)"
            );
        }
    };

    static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建实验数据字段表
            database.execSQL("CREATE TABLE IF NOT EXISTS experiment_data_fields (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "field_name TEXT NOT NULL," +
                    "field_key TEXT NOT NULL," +
                    "unit TEXT NOT NULL," +
                    "precision REAL NOT NULL," +
                    "experiment_type_id INTEGER NOT NULL," +
                    "FOREIGN KEY(experiment_type_id) REFERENCES experiment_types(id) ON DELETE CASCADE)");
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_experiment_data_fields_experiment_type_id " +
                    "ON experiment_data_fields(experiment_type_id)");
        }
    };

    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Running migration from version 19 to version 20");
            database.execSQL("ALTER TABLE specimens ADD COLUMN specimen_company TEXT");
        }
    };

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // 初始化默认的实验类型数据
            new Thread(() -> {
                try {
                    AppDatabase database = INSTANCE;
                    if (database != null) {
                        ExperimentTypeDao experimentTypeDao = database.experimentTypeDao();
                        ExperimentDataFieldDao fieldDao = database.experimentDataFieldDao();
                        
                        // 检查是否已经有数据
                        if (experimentTypeDao.getCount() == 0) {
                            // 创建实验类型
                            List<ExperimentType> types = Arrays.asList(
                                // 针入度试验
                                new ExperimentType(
                                    "针入度试验",
                                    "penetration",
                                    "ASPHALT"
                                ),
                                
                                // 延度试验
                                new ExperimentType(
                                    "延度试验",
                                    "ductility",
                                    "ASPHALT"
                                ),
                                
                                // 软化点试验
                                new ExperimentType(
                                    "软化点试验（环球法）",
                                    "softening_point",
                                    "ASPHALT"
                                ),

                                // 马歇尔稳定度试验
                                new ExperimentType(
                                    "马歇尔稳定度试验",
                                    "marshall_stability",
                                    "MIXTURE"
                                ),

                                // 理论最大相对密度试验
                                new ExperimentType(
                                    "理论最大相对密度试验",
                                    "theoretical_density",
                                    "MIXTURE"
                                ),

                                // 体积密度试验
                                new ExperimentType(
                                    "体积密度试验",
                                    "bulk_density",
                                    "MIXTURE"
                                ),

                                // 空隙率试验
                                new ExperimentType(
                                    "空隙率试验",
                                    "void_ratio",
                                    "MIXTURE"
                                ),

                                // 飞散试验
                                new ExperimentType(
                                    "飞散试验",
                                    "cantabro",
                                    "MIXTURE"
                                ),

                                // 动稳定度试验
                                new ExperimentType(
                                    "动稳定度试验",
                                    "dynamic_stability",
                                    "MIXTURE"
                                ),

                                // 车辙试验
                                new ExperimentType(
                                    "沥青混合料车辙实验（汉堡车辙）",
                                    "hamburg_wheel_tracking",
                                    "MIXTURE"
                                )
                            );
                            
                            // 插入实验类型并获取生成的ID
                            List<Long> typeIds = experimentTypeDao.insertAll(types);
                            Log.i(TAG, "Initialized experiment types data");

                            // 为每个实验类型创建数据字段
                            for (int i = 0; i < types.size(); i++) {
                                ExperimentType type = types.get(i);
                                long typeId = typeIds.get(i);
                                List<ExperimentDataField> fields = new ArrayList<>();
                                
                                switch (type.getType()) {
                                    case "penetration":
                                        fields.add(new ExperimentDataField(
                                            "温度", "temperature", "℃", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "针入度", "penetration", "mm", 0.1, typeId
                                        ));
                                        break;
                                        
                                    case "ductility":
                                        fields.add(new ExperimentDataField(
                                            "温度", "temperature", "℃", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "延度", "ductility", "cm", 0.1, typeId
                                        ));
                                        break;
                                        
                                    case "softening_point":
                                        fields.add(new ExperimentDataField(
                                            "温度", "temperature", "℃", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "软化点", "softening_point", "℃", 0.5, typeId
                                        ));
                                        break;

                                    case "marshall_stability":
                                        fields.add(new ExperimentDataField(
                                            "稳定度1", "stability_1", "kN", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "稳定度2", "stability_2", "kN", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "稳定度3", "stability_3", "kN", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "流值1", "flow_1", "mm", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "流值2", "flow_2", "mm", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "流值3", "flow_3", "mm", 0.1, typeId
                                        ));
                                        break;

                                    case "hamburg_wheel_tracking":
                                        fields.add(new ExperimentDataField(
                                            "第一稳态曲线斜率", "first_steady_slope", "mm/cycle", 0.001, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "第一稳态曲线截距", "first_steady_intercept", "mm", 0.1, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "第二稳态曲线斜率", "second_steady_slope", "mm/cycle", 0.001, typeId
                                        ));
                                        fields.add(new ExperimentDataField(
                                            "第二稳态曲线截距", "second_steady_intercept", "mm", 0.1, typeId
                                        ));
                                        break;

                                    case "theoretical_density":
                                        fields.add(new ExperimentDataField(
                                            "理论最大相对密度", "theoretical_density", "g/cm³", 0.001, typeId
                                        ));
                                        break;

                                    case "bulk_density":
                                        fields.add(new ExperimentDataField(
                                            "体积密度", "bulk_density", "g/cm³", 0.001, typeId
                                        ));
                                        break;

                                    case "void_ratio":
                                        fields.add(new ExperimentDataField(
                                            "空隙率", "void_ratio", "%", 0.1, typeId
                                        ));
                                        break;

                                    case "cantabro":
                                        fields.add(new ExperimentDataField(
                                            "飞散损失率", "cantabro_loss", "%", 0.1, typeId
                                        ));
                                        break;

                                    case "dynamic_stability":
                                        fields.add(new ExperimentDataField(
                                            "动稳定度", "dynamic_stability", "次/mm", 1, typeId
                                        ));
                                        break;
                                }
                                
                                if (!fields.isEmpty()) {
                                    fieldDao.insertAll(fields);
                                }
                            }
                            Log.i(TAG, "Initialized experiment data fields");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing database", e);
                }
            }).start();
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                            MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                            MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                            MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
                            MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                    .addCallback(roomCallback)
                    // 如果数据库升级失败，允许重建数据库
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}