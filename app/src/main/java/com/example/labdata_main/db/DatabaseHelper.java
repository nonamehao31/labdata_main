package com.example.labdata_main.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.labdata_main.model.Equipment;
import com.example.labdata_main.model.Experimenter;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类
 * 用于管理用户数据的SQLite数据库操作
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    
    // 数据库名称和版本
    private static final String DATABASE_NAME = "UserDB";

    // 用户表
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_COMPANY = "company";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    
    // 添加用户类型列常量
    private static final String COLUMN_USER_TYPE = "user_type";

    // 权限相关的表
    private static final String TABLE_PERMISSIONS = "permissions";
    private static final String COLUMN_EXPERIMENTER_ID = "experimenter_id";
    private static final String COLUMN_CAN_ASSIGN_TASKS = "can_assign_tasks";
    private static final String COLUMN_CAN_MANAGE_DEVICES = "can_manage_devices";

    private static final String TABLE_PROJECT_ACCESS = "project_access";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_HAS_ACCESS = "has_access";

    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_PROJECT_NAME = "project_name";
    private static final String COLUMN_DEADLINE = "deadline";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_IS_ACCESSIBLE = "is_accessible";

    // 设备表
    private static final String TABLE_EQUIPMENT = "equipment";
    private static final String COLUMN_COMPANY_ID = "company_id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_MODEL = "model";
    private static final String COLUMN_MANUFACTURER = "manufacturer";
    private static final String COLUMN_PURCHASE_YEAR = "purchase_year";

    // 配比表
    private static final String TABLE_MIX_RATIOS = "mix_ratios";
    private static final String COLUMN_MIX_RATIO_ID = "id";
    private static final String COLUMN_MIX_RATIO_NAME = "name";
    private static final String COLUMN_CREATION_TIME = "creation_time";

    // 配比材料关联表
    private static final String TABLE_MIX_RATIO_MATERIALS = "mix_ratio_materials";
    private static final String COLUMN_MATERIAL_NAME = "material_name";
    private static final String COLUMN_MATERIAL_PERCENTAGE = "percentage";
    private static final String COLUMN_MATERIAL_TYPE = "material_type";

    // 修改数据库版本号，触发升级
    private static final int DATABASE_VERSION = 5; // 从4升级到5
    
    // 创建用户表的 SQL
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_COMPANY + " TEXT,"  // 移除 UNIQUE 约束
            + COLUMN_NAME + " TEXT,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_USER_TYPE + " INTEGER DEFAULT 0"
            + ")";

    // 创建设备表的 SQL
    private static final String CREATE_EQUIPMENT_TABLE = "CREATE TABLE " + TABLE_EQUIPMENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COMPANY_ID + " TEXT NOT NULL, "
            + COLUMN_TYPE + " TEXT NOT NULL, "
            + COLUMN_MODEL + " TEXT NOT NULL, "
            + COLUMN_MANUFACTURER + " TEXT NOT NULL, "
            + COLUMN_PURCHASE_YEAR + " TEXT NOT NULL, "
            + "FOREIGN KEY(" + COLUMN_COMPANY_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_COMPANY + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE)";

    // 创建权限表的 SQL
    private static final String CREATE_PERMISSIONS_TABLE = "CREATE TABLE " + TABLE_PERMISSIONS + "("
            + COLUMN_EXPERIMENTER_ID + " INTEGER,"
            + COLUMN_CAN_ASSIGN_TASKS + " INTEGER DEFAULT 0,"
            + COLUMN_CAN_MANAGE_DEVICES + " INTEGER DEFAULT 0,"
            + "PRIMARY KEY (" + COLUMN_EXPERIMENTER_ID + "))";

    // 创建项目表的 SQL
    private static final String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
            + COLUMN_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PROJECT_NAME + " TEXT NOT NULL,"
            + COLUMN_DEADLINE + " TEXT,"
            + COLUMN_CREATE_TIME + " INTEGER,"
            + COLUMN_IS_ACCESSIBLE + " INTEGER DEFAULT 1)";

    // 创建项目访问权限表的 SQL
    private static final String CREATE_PROJECT_ACCESS_TABLE = "CREATE TABLE " + TABLE_PROJECT_ACCESS + "("
            + COLUMN_EXPERIMENTER_ID + " INTEGER,"
            + COLUMN_PROJECT_ID + " INTEGER,"
            + COLUMN_HAS_ACCESS + " INTEGER DEFAULT 0,"
            + "PRIMARY KEY (" + COLUMN_EXPERIMENTER_ID + ", " + COLUMN_PROJECT_ID + "))";

    // 创建配比表的 SQL
    private static final String CREATE_MIX_RATIOS_TABLE = "CREATE TABLE " + TABLE_MIX_RATIOS + "("
            + COLUMN_MIX_RATIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_MIX_RATIO_NAME + " TEXT NOT NULL,"
            + COLUMN_CREATION_TIME + " INTEGER NOT NULL"
            + ")";

    // 创建配比材料关联表的 SQL
    private static final String CREATE_MIX_RATIO_MATERIALS_TABLE = "CREATE TABLE " + TABLE_MIX_RATIO_MATERIALS + "("
            + COLUMN_MIX_RATIO_ID + " INTEGER NOT NULL,"
            + COLUMN_MATERIAL_NAME + " TEXT NOT NULL,"
            + COLUMN_MATERIAL_PERCENTAGE + " REAL NOT NULL,"
            + COLUMN_MATERIAL_TYPE + " TEXT NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_MIX_RATIO_ID + ") REFERENCES " + TABLE_MIX_RATIOS + "(" + COLUMN_MIX_RATIO_ID + ") ON DELETE CASCADE,"
            + "PRIMARY KEY(" + COLUMN_MIX_RATIO_ID + ", " + COLUMN_MATERIAL_NAME + ")"
            + ")";

    /**
     * 构造函数
     * @param context 应用上下文
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        try {
            db.execSQL("PRAGMA foreign_keys=ON");
            // 创建用户表
            db.execSQL(CREATE_USERS_TABLE);
            Log.d(TAG, "Users table created successfully");
            
            // 创建设备表
            db.execSQL(CREATE_EQUIPMENT_TABLE);
            Log.d(TAG, "Equipment table created successfully");

            // 创建权限表
            db.execSQL(CREATE_PERMISSIONS_TABLE);
            Log.d(TAG, "Permissions table created successfully");

            // 创建项目表
            db.execSQL(CREATE_PROJECTS_TABLE);
            Log.d(TAG, "Projects table created successfully");

            // 创建项目访问权限表
            db.execSQL(CREATE_PROJECT_ACCESS_TABLE);
            Log.d(TAG, "Project access table created successfully");

            // 创建配比表
            db.execSQL(CREATE_MIX_RATIOS_TABLE);
            Log.d(TAG, "Mix ratios table created successfully");

            // 创建配比材料关联表
            db.execSQL(CREATE_MIX_RATIO_MATERIALS_TABLE);
            Log.d(TAG, "Mix ratio materials table created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    
        // 检查是否需要从版本2升级到版本3
        if (oldVersion < 3) {
           try {
               // 启用外键支持
               db.execSQL("PRAGMA foreign_keys=ON");
            
               // 添加用户类型列，默认值为0（普通用户）
               String alterTableSQL = "ALTER TABLE " + TABLE_USERS + 
                                      " ADD COLUMN " + COLUMN_USER_TYPE + 
                                      " INTEGER DEFAULT 0";
            
                // 执行添加列的SQL语句
                db.execSQL(alterTableSQL);
            
                // 记录成功日志
                Log.d(TAG, "Successfully added user_type column to users table");
            
            } catch (Exception e) {
                // 记录错误日志
                Log.e(TAG, "Error during database upgrade: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 5) {
            try {
                // 创建临时表
                db.execSQL("CREATE TABLE users_temp ("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_COMPANY + " TEXT,"
                        + COLUMN_NAME + " TEXT,"
                        + COLUMN_PHONE + " TEXT,"
                        + COLUMN_EMAIL + " TEXT UNIQUE,"
                        + COLUMN_PASSWORD + " TEXT,"
                        + COLUMN_USER_TYPE + " INTEGER DEFAULT 0"
                        + ")");

                // 复制数据到临时表
                db.execSQL("INSERT INTO users_temp SELECT * FROM " + TABLE_USERS);

                // 删除旧表
                db.execSQL("DROP TABLE " + TABLE_USERS);

                // 将临时表重命名为正式表
                db.execSQL("ALTER TABLE users_temp RENAME TO " + TABLE_USERS);

                Log.d(TAG, "Successfully upgraded users table to version 5");
            } catch (Exception e) {
                Log.e(TAG, "Error during database upgrade: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * 添加新用户
     * @param user 用户对象
     * @return 插入成功返回新记录的ID，失败返回-1
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        
        try {
            ContentValues values = new ContentValues();
            // 准备要插入的数据
            values.put(COLUMN_COMPANY, user.getCompany());
            values.put(COLUMN_NAME, user.getName());
            values.put(COLUMN_PHONE, user.getPhone());
            values.put(COLUMN_EMAIL, user.getEmail());
            values.put(COLUMN_PASSWORD, user.getPassword());
            values.put(COLUMN_USER_TYPE, user.getUserType());

            // 插入数据并获取返回值
            id = db.insert(TABLE_USERS, null, values);
            
            if (id == -1) {
                Log.e(TAG, "Failed to insert user. Email: " + user.getEmail() + ", Company: " + user.getCompany());
            } else {
                Log.d(TAG, "Successfully inserted user with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
        return id;
    }

    /**
     * 验证用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @return 验证成功返回用户对象，失败返回null
     */
    public User checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            String[] columns = {
                COLUMN_ID,
                COLUMN_COMPANY,
                COLUMN_NAME,
                COLUMN_PHONE,
                COLUMN_EMAIL,
                COLUMN_PASSWORD,
                COLUMN_USER_TYPE
            };

            String selection = COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            android.util.Log.d(TAG, "Checking user with email: " + email);

            Cursor cursor = db.query(TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                String storedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                if (password.equals(storedPassword)) {
                    String company = cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY));
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE));
                    int userType = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_TYPE));
                    
                    android.util.Log.d(TAG, "User found. User type from database: " + userType);
                    
                    user = new User(company, name, phone, email, password, userType);
                    user.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    
                    android.util.Log.d(TAG, "User object created. Verifying user type: " + user.getUserType());
                }
                cursor.close();
            } else {
                android.util.Log.d(TAG, "No user found with email: " + email);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error checking user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }

        return user;
    }

    /**
     * 根据邮箱获取用户信息
     * @param email 用户邮箱
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            String[] columns = {
                COLUMN_ID,
                COLUMN_COMPANY,
                COLUMN_NAME,
                COLUMN_PHONE,
                COLUMN_EMAIL,
                COLUMN_PASSWORD,
                COLUMN_USER_TYPE
            };

            String selection = COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            android.util.Log.d(TAG, "Fetching user with email: " + email);

            Cursor cursor = db.query(TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                String company = cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                int userType = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_TYPE));
                
                user = new User(company, name, phone, email, password, userType);
                user.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                
                android.util.Log.d(TAG, "User found. ID: " + user.getId() + ", Type: " + user.getUserType());
                cursor.close();
            } else {
                android.util.Log.d(TAG, "No user found with email: " + email);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error fetching user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }

        return user;
    }

    /**
     * 检查邮箱是否已被注册
     * @param email 要检查的邮箱
     * @return true表示邮箱已存在，false表示邮箱可用
     */
    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 查询是否存在使用该邮箱的用户
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 添加设备
    public long addEquipment(Equipment equipment) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        
        try {
            // 先检查公司ID是否存在
            String query = "SELECT " + COLUMN_COMPANY + " FROM " + TABLE_USERS + 
                         " WHERE " + COLUMN_COMPANY + "=?";
            Cursor cursor = db.rawQuery(query, new String[]{equipment.getCompanyId()});
            
            Log.d(TAG, "Checking company ID: " + equipment.getCompanyId());
            Log.d(TAG, "Company exists: " + (cursor != null && cursor.getCount() > 0));
            
            if (cursor != null && cursor.getCount() > 0) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_COMPANY_ID, equipment.getCompanyId());
                values.put(COLUMN_TYPE, equipment.getType());
                values.put(COLUMN_MODEL, equipment.getModel());
                values.put(COLUMN_MANUFACTURER, equipment.getManufacturer());
                values.put(COLUMN_PURCHASE_YEAR, equipment.getPurchaseYear());
                
                Log.d(TAG, "Inserting equipment: " + 
                    "Type=" + equipment.getType() + 
                    ", Model=" + equipment.getModel() + 
                    ", Manufacturer=" + equipment.getManufacturer() + 
                    ", Year=" + equipment.getPurchaseYear());
                
                result = db.insert(TABLE_EQUIPMENT, null, values);
                Log.d(TAG, "Insert result: " + result);
            }
            
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding equipment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
        
        return result;
    }

    // 用于调试：获取所有用户
    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);
        
        Log.d(TAG, "All users in database:");
        if (cursor.moveToFirst()) {
            do {
                String company = cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY));
                String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                Log.d(TAG, "Company: " + company + ", Email: " + email);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    // 检查公司是否已初始化设备
    public boolean hasInitializedEquipment(String companyId) {
        if (companyId == null || companyId.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] types = {"MIXING", "FORMING", "TESTING"};
        
        try {
            for (String type : types) {
                String query = "SELECT COUNT(*) FROM " + TABLE_EQUIPMENT + 
                              " WHERE company_id = ? AND type = ?";
                Cursor cursor = db.rawQuery(query, new String[]{companyId, type});
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    cursor.close();
                    if (count == 0) {
                        return false;
                    }
                } else {
                    cursor.close();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取公司的所有设备
    public List<Equipment> getCompanyEquipment(String companyId) {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_EQUIPMENT + " WHERE company_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{companyId});
        
        if (cursor.moveToFirst()) {
            do {
                Equipment equipment = new Equipment();
                equipment.setId(cursor.getInt(cursor.getColumnIndex("id")));
                equipment.setCompanyId(cursor.getString(cursor.getColumnIndex("company_id")));
                equipment.setType(cursor.getString(cursor.getColumnIndex("type")));
                equipment.setModel(cursor.getString(cursor.getColumnIndex("model")));
                equipment.setManufacturer(cursor.getString(cursor.getColumnIndex("manufacturer")));
                equipment.setPurchaseYear(cursor.getString(cursor.getColumnIndex("purchase_year")));
                equipmentList.add(equipment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return equipmentList;
    }

    // 根据公司ID获取所有设备
    public List<Equipment> getEquipmentsByCompanyId(String companyId) {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            Log.d(TAG, "Getting equipment for company: " + companyId);
            
            cursor = db.query(TABLE_EQUIPMENT,
                null,  // 获取所有列
                COLUMN_COMPANY_ID + "=?",
                new String[]{companyId},
                null, null, null);

            Log.d(TAG, "Query executed, cursor count: " + (cursor != null ? cursor.getCount() : "null"));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Equipment equipment = new Equipment();
                    equipment.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    equipment.setCompanyId(cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY_ID)));
                    equipment.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    equipment.setModel(cursor.getString(cursor.getColumnIndex(COLUMN_MODEL)));
                    equipment.setManufacturer(cursor.getString(cursor.getColumnIndex(COLUMN_MANUFACTURER)));
                    equipment.setPurchaseYear(cursor.getString(cursor.getColumnIndex(COLUMN_PURCHASE_YEAR)));
                    equipmentList.add(equipment);
                    
                    Log.d(TAG, String.format("Added equipment: ID=%d, Type=%s, Model=%s",
                        equipment.getId(), equipment.getType(), equipment.getModel()));
                } while (cursor.moveToNext());
            } else {
                Log.w(TAG, "No equipment found for company: " + companyId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting equipment: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        Log.d(TAG, "Returning equipment list with size: " + equipmentList.size());
        return equipmentList;
    }

    // 根据邮箱和用户类型查询用户
    public User getUserByEmailAndType(String email, int userType) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = null;

        try {
            String[] columns = {
                COLUMN_ID, COLUMN_COMPANY, COLUMN_NAME, 
                COLUMN_PHONE, COLUMN_EMAIL, COLUMN_PASSWORD, 
                COLUMN_USER_TYPE
            };
            
            String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_USER_TYPE + " = ?";
            String[] selectionArgs = {email, String.valueOf(userType)};
            
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, 
                           null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                user.setCompany(cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY)));
                user.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                user.setUserType(cursor.getInt(cursor.getColumnIndex(COLUMN_USER_TYPE)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying user by email and type: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    /**
     * 根据邮箱获取用户类型
     * @param email 用户邮箱
     * @return 用户类型（0表示实验员，1表示管理员）
     */
    public int getUserTypeByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userType = 0;  // 默认为实验员

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_TYPE},
                COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userType = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return userType;
    }

    // 获取所有实验员
    public List<Experimenter> getAllExperimenters() {
        List<Experimenter> experimenters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NAME},
                COLUMN_USER_TYPE + "=?",
                new String[]{"0"},  // 0 表示实验员
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                Experimenter experimenter = new Experimenter(id, name);
                
                // 获取权限信息
                Cursor permissionCursor = db.query(TABLE_PERMISSIONS,
                        new String[]{COLUMN_CAN_ASSIGN_TASKS, COLUMN_CAN_MANAGE_DEVICES},
                        COLUMN_EXPERIMENTER_ID + "=?",
                        new String[]{String.valueOf(id)},
                        null, null, null);

                if (permissionCursor != null && permissionCursor.moveToFirst()) {
                    experimenter.setCanAssignTasks(permissionCursor.getInt(0) == 1);
                    experimenter.setCanManageDevices(permissionCursor.getInt(1) == 1);
                    permissionCursor.close();
                }

                experimenters.add(experimenter);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return experimenters;
    }

    // 更新实验员权限
    public void updateExperimenterPermissions(int experimenterId, boolean canAssignTasks, boolean canManageDevices) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAN_ASSIGN_TASKS, canAssignTasks ? 1 : 0);
        values.put(COLUMN_CAN_MANAGE_DEVICES, canManageDevices ? 1 : 0);

        db.replace(TABLE_PERMISSIONS,
                null,
                values);
    }

    // 获取所有项目
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROJECTS,
                new String[]{COLUMN_PROJECT_ID, COLUMN_PROJECT_NAME, COLUMN_DEADLINE, COLUMN_CREATE_TIME, COLUMN_IS_ACCESSIBLE},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String deadline = cursor.getString(2);
                long createTime = cursor.getLong(3);
                boolean isAccessible = cursor.getInt(4) == 1;
                projects.add(new Project(id, name, deadline, createTime, isAccessible)); 
            } while (cursor.moveToNext());
            cursor.close();
        }
        return projects;
    }

    // 获取实验员的项目访问权限
    public List<Project> getExperimenterProjects(int experimenterId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p." + COLUMN_PROJECT_ID + ", p." + COLUMN_PROJECT_NAME + 
                      ", p." + COLUMN_DEADLINE + ", p." + COLUMN_CREATE_TIME + ", p." + COLUMN_IS_ACCESSIBLE + 
                      ", COALESCE(pa." + COLUMN_HAS_ACCESS + ", 0) as has_access" +
                      " FROM " + TABLE_PROJECTS + " p" +
                      " LEFT JOIN " + TABLE_PROJECT_ACCESS + " pa" +
                      " ON p." + COLUMN_PROJECT_ID + " = pa." + COLUMN_PROJECT_ID +
                      " AND pa." + COLUMN_EXPERIMENTER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(experimenterId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String deadline = cursor.getString(2);
                long createTime = cursor.getLong(3);
                boolean isAccessible = cursor.getInt(4) == 1;
                boolean hasAccess = cursor.getInt(5) == 1;
                projects.add(new Project(id, name, deadline, createTime, isAccessible, hasAccess));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return projects;
    }

    // 更新项目访问权限
    public void updateProjectAccess(int experimenterId, int projectId, boolean hasAccess) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPERIMENTER_ID, experimenterId);
        values.put(COLUMN_PROJECT_ID, projectId);
        values.put(COLUMN_HAS_ACCESS, hasAccess ? 1 : 0);

        db.replace(TABLE_PROJECT_ACCESS,
                null,
                values);
    }

    // 插入新项目
    public long insertProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_NAME, project.getName());
        values.put(COLUMN_DEADLINE, project.getDeadline());
        values.put(COLUMN_CREATE_TIME, project.getCreateTime());
        values.put(COLUMN_IS_ACCESSIBLE, project.isAccessible() ? 1 : 0);
        return db.insert(TABLE_PROJECTS, null, values);
    }

    // 获取可访问的项目列表
    public List<Project> getAccessibleProjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Project> projects = new ArrayList<>();

        String query = "SELECT " + COLUMN_PROJECT_ID + ", " + COLUMN_PROJECT_NAME + 
                      ", " + COLUMN_DEADLINE + ", " + COLUMN_CREATE_TIME + ", " + COLUMN_IS_ACCESSIBLE +
                      " FROM " + TABLE_PROJECTS +
                      " WHERE " + COLUMN_IS_ACCESSIBLE + " = 1" +
                      " ORDER BY " + COLUMN_CREATE_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String deadline = cursor.getString(2);
                long createTime = cursor.getLong(3);
                boolean isAccessible = cursor.getInt(4) == 1;
                projects.add(new Project(id, name, deadline, createTime, isAccessible));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return projects;
    }

    // 获取所有配比列表
    public List<MixRatio> getMixRatios() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<MixRatio> mixRatios = new ArrayList<>();

        String query = "SELECT * FROM mix_ratios ORDER BY creation_time DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MixRatio mixRatio = new MixRatio();
                mixRatio.setId(cursor.getLong(cursor.getColumnIndex("id")));
                mixRatio.setName(cursor.getString(cursor.getColumnIndex("name")));
                mixRatio.setCreationTime(cursor.getLong(cursor.getColumnIndex("creation_time")));
                
                // 获取材料列表
                List<com.example.labdata_main.model.MaterialItem> materials = new ArrayList<>();
                
                // 从数据库中读取材料信息并创建MaterialItem对象
                // 假设我们有一个关联表 mix_ratio_materials 存储材料信息
                String materialQuery = "SELECT * FROM mix_ratio_materials WHERE mix_ratio_id = ?";
                Cursor materialCursor = db.rawQuery(materialQuery, 
                    new String[]{String.valueOf(mixRatio.getId())});
                
                if (materialCursor != null && materialCursor.moveToFirst()) {
                    do {
                        String name = materialCursor.getString(
                            materialCursor.getColumnIndex("material_name"));
                        float percentage = materialCursor.getFloat(
                            materialCursor.getColumnIndex("percentage"));
                        String type = materialCursor.getString(
                            materialCursor.getColumnIndex("material_type"));
                        
                        materials.add(new com.example.labdata_main.model.MaterialItem(name, percentage, type));
                    } while (materialCursor.moveToNext());
                    materialCursor.close();
                }
                
                mixRatio.setMaterials(materials);
                mixRatios.add(mixRatio);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return mixRatios;
    }

    /**
     * 删除项目
     * @param projectId 要删除的项目ID
     * @return 是否删除成功
     */
    public boolean deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 开始事务
            db.beginTransaction();
            
            // 删除项目访问权限记录
            db.delete(TABLE_PROJECT_ACCESS, COLUMN_PROJECT_ID + " = ?", 
                    new String[]{String.valueOf(projectId)});
            
            // 删除项目
            int result = db.delete(TABLE_PROJECTS, COLUMN_PROJECT_ID + " = ?", 
                    new String[]{String.valueOf(projectId)});
            
            // 提交事务
            db.setTransactionSuccessful();
            
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting project: " + e.getMessage());
            return false;
        } finally {
            // 结束事务
            db.endTransaction();
        }
    }

    /**
     * 批量更新项目访问权限
     * @param projects 要更新的项目列表
     * @return 是否更新成功
     */
    public boolean updateProjectsAccess(List<Project> projects) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            
            for (Project project : projects) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_IS_ACCESSIBLE, project.isAccessible() ? 1 : 0);
                
                db.update(TABLE_PROJECTS, 
                         values,
                         COLUMN_PROJECT_ID + " = ?",
                         new String[]{String.valueOf(project.getId())});
            }
            
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating projects access: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 清空项目表中的所有项目
     * 用于与后端同步时先清空本地数据
     * @return 影响的行数
     */
    public int clearProjects() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PROJECTS, null, null);
        db.close();
        return result;
    }
}
