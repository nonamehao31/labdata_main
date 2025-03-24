package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.labdata_main.database.Converters;
import java.util.Map;
import java.util.HashMap;

@Entity(tableName = "asphalt_experiment_data")
public class AsphaltExperimentData {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long taskId;
    private String experimentType; // 实验类型
    private String deviceCode;    // 设备编号
    private String deviceManufacturer; // 设备制造商
    private String deviceModel;   // 设备型号
    private long createTime;      // 创建时间
    private String experimenter;  // 实验人员
    @TypeConverters(Converters.class)
    private Map<String, String> experimentValues; // 存储实验数据键值对

    public AsphaltExperimentData() {
        experimentValues = new HashMap<>();
        createTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
    
    public String getExperimentType() { return experimentType; }
    public void setExperimentType(String experimentType) { this.experimentType = experimentType; }
    
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    
    public String getDeviceManufacturer() { return deviceManufacturer; }
    public void setDeviceManufacturer(String deviceManufacturer) { this.deviceManufacturer = deviceManufacturer; }
    
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
    
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    
    public String getExperimenter() { return experimenter; }
    public void setExperimenter(String experimenter) { this.experimenter = experimenter; }
    
    public Map<String, String> getExperimentValues() { return experimentValues; }
    public void setExperimentValues(Map<String, String> experimentValues) { 
        this.experimentValues = experimentValues; 
    }

    // 实验类型常量
    public static final String TYPE_DENSITY = "density_relative_density";
    public static final String TYPE_PENETRATION = "penetration";
    public static final String TYPE_DUCTILITY = "ductility";
    public static final String TYPE_SOFTENING_POINT = "softening_point";
    public static final String TYPE_TFOT = "tfot";
    public static final String TYPE_RTFOT = "rtfot";
    public static final String TYPE_FLASH_POINT = "flash_point";
    public static final String TYPE_VISCOSITY = "viscosity";
    public static final String TYPE_BBR = "bbr";
    public static final String TYPE_DSR = "dsr";
    public static final String TYPE_DTT = "dtt";
    public static final String TYPE_PAV = "pav";
    public static final String TYPE_MSCR = "mscr";
    public static final String TYPE_FORCE_DUCTILITY = "force_ductility";
    public static final String TYPE_BROOKFIELD_VISCOSITY = "brookfield_viscosity";

    // 数据字段常量
    public static class Fields {
        // 通用字段
        public static final String TEMPERATURE = "temperature";
        public static final String DEVICE_CODE = "device_code";
        
        // 密度与相对密度试验
        public static class Density {
            public static final String M1 = "m1"; // 干净比重瓶质量
            public static final String M2 = "m2"; // 比重瓶和水的质量
            public static final String M3 = "m3"; // 比重瓶和沥青的质量
            public static final String M4 = "m4"; // 比重瓶和黏稠沥青试样的质量
            public static final String M5 = "m5"; // 比重瓶和黏稠沥青试样和水的质量
            public static final String M6 = "m6"; // 比重瓶和固体沥青试样的质量
            public static final String M7 = "m7"; // 比重瓶和固体沥青试样和水的质量
        }

        // 针入度试验
        public static class Penetration {
            public static final String READING = "reading"; // 读数，单位mm
        }

        // 软化点试验
        public static class SofteningPoint {
            public static final String TEMPERATURE = "temperature"; // 初始温度
            public static final String SOFTENING_TEMPERATURE = "softening_temperature"; // 软化温度
        }

        // 延度试验
        public static class Ductility {
            public static final String DISPLACEMENT = "displacement"; // 拉长位移，单位cm
        }

        // 薄膜加热试验
        public static class TFOT {
            public static final String M0 = "m0"; // 盛样皿质量
            public static final String M1 = "m1"; // 加热前质量
            public static final String M2 = "m2"; // 加热后质量
        }

        // 旋转薄膜加热试验
        public static class RTFOT {
            public static final String M0 = "m0"; // 盛样瓶质量
            public static final String M1 = "m1"; // 加热前质量
            public static final String M2 = "m2"; // 加热后质量
        }

        // 闪点与燃点试验
        public static class FlashPoint {
            public static final String HEATING_RATE1 = "heating_rate1"; // 升温速度1
            public static final String HEATING_RATE2 = "heating_rate2"; // 升温速度2
        }

        // 旋转黏度试验
        public static class Viscosity {
            public static final String ROTOR_TYPE = "rotor_type"; // 转子型号
            public static final String ROTOR_SPEED = "rotor_speed"; // 转子速率
        }

        // 弯曲蠕变劲度试验
        public static class BBR {
            // 试件尺寸
            public static final String BEAM_SPAN = "beam_span"; // 弯曲梁跨度(两支点间的距离，mm)
            public static final String SPECIMEN_WIDTH = "specimen_width"; // 试件宽度(mm)
            public static final String SPECIMEN_HEIGHT = "specimen_height"; // 试件高度(mm)
            
            // 时间点数据 - 8秒
            public static final String TEMPERATURE_8S = "temperature_8s"; // 8秒时的实验温度(°C)
            public static final String LOAD_8S = "load_8s"; // 8秒时的施加荷载(N)
            public static final String DEFLECTION_8S = "deflection_8s"; // 8秒时的变形挠度(mm)
            public static final String STIFFNESS_8S = "stiffness_8s"; // 8秒时的弯曲蠕变劲度模量(MPa)
            
            // 时间点数据 - 15秒
            public static final String TEMPERATURE_15S = "temperature_15s"; // 15秒时的实验温度(°C)
            public static final String LOAD_15S = "load_15s"; // 15秒时的施加荷载(N)
            public static final String DEFLECTION_15S = "deflection_15s"; // 15秒时的变形挠度(mm)
            public static final String STIFFNESS_15S = "stiffness_15s"; // 15秒时的弯曲蠕变劲度模量(MPa)
            
            // 时间点数据 - 30秒
            public static final String TEMPERATURE_30S = "temperature_30s"; // 30秒时的实验温度(°C)
            public static final String LOAD_30S = "load_30s"; // 30秒时的施加荷载(N)
            public static final String DEFLECTION_30S = "deflection_30s"; // 30秒时的变形挠度(mm)
            public static final String STIFFNESS_30S = "stiffness_30s"; // 30秒时的弯曲蠕变劲度模量(MPa)
            
            // 时间点数据 - 60秒
            public static final String TEMPERATURE_60S = "temperature_60s"; // 60秒时的实验温度(°C)
            public static final String LOAD_60S = "load_60s"; // 60秒时的施加荷载(N)
            public static final String DEFLECTION_60S = "deflection_60s"; // 60秒时的变形挠度(mm)
            public static final String STIFFNESS_60S = "stiffness_60s"; // 60秒时的弯曲蠕变劲度模量(MPa)
            
            // 时间点数据 - 120秒
            public static final String TEMPERATURE_120S = "temperature_120s"; // 120秒时的实验温度(°C)
            public static final String LOAD_120S = "load_120s"; // 120秒时的施加荷载(N)
            public static final String DEFLECTION_120S = "deflection_120s"; // 120秒时的变形挠度(mm)
            public static final String STIFFNESS_120S = "stiffness_120s"; // 120秒时的弯曲蠕变劲度模量(MPa)
            
            // 时间点数据 - 240秒
            public static final String TEMPERATURE_240S = "temperature_240s"; // 240秒时的实验温度(°C)
            public static final String LOAD_240S = "load_240s"; // 240秒时的施加荷载(N)
            public static final String DEFLECTION_240S = "deflection_240s"; // 240秒时的变形挠度(mm)
            public static final String STIFFNESS_240S = "stiffness_240s"; // 240秒时的弯曲蠕变劲度模量(MPa)
            
            // 计算结果
            public static final String CREEP_RATE = "creep_rate"; // 蠕变速率(m值)
        }

        // 动态剪切流变试验
        public static class DSR {
            public static final String CONTROL_MODE = "control_mode"; // 控制方式
            public static final String STRESS = "stress"; // 应力值
            public static final String FREQUENCY = "frequency"; // 试验频率
            public static final String COMPLEX_MODULUS = "complex_modulus"; // 复合模量G*
            public static final String PHASE_ANGLE = "phase_angle"; // 相位角
            public static final String PLATE_RADIUS = "plate_radius"; // 试验板半径(R)
            public static final String PLATE_GAP = "plate_gap"; // 试验平板间距(h)
            public static final String MAX_SHEAR_STRESS = "max_shear_stress"; // 最大剪切应力(τmax)
            public static final String MAX_SHEAR_STRAIN = "max_shear_strain"; // 最大剪切应变(γmax)
            public static final String TEMPERATURE = "temperature"; // 试验温度
            // 动态添加的温度点数据前缀
            public static final String TEMPERATURE_PREFIX = "temperature_"; // 试验温度前缀
            public static final String FREQUENCY_PREFIX = "frequency_"; // 试验频率前缀
            public static final String MAX_SHEAR_STRESS_PREFIX = "max_shear_stress_"; // 最大剪切应力前缀
            public static final String MAX_SHEAR_STRAIN_PREFIX = "max_shear_strain_"; // 最大剪切应变前缀
            public static final String PHASE_ANGLE_PREFIX = "phase_angle_"; // 相位角前缀
            public static final String COMPLEX_MODULUS_PREFIX = "complex_modulus_"; // 复合模量前缀
        }

        // 直接拉伸试验
        public static class DTT {
            public static final String FAILURE_LOAD = "failure_load"; // 破坏荷载
            public static final String CROSS_SECTION = "cross_section"; // 横断面积
            public static final String ELONGATION = "elongation"; // 伸长值
            public static final String EFFECTIVE_LENGTH = "effective_length"; // 有效拉伸长度
            public static final String FAILURE_STRESS = "failure_stress"; // 破坏应力
            public static final String FAILURE_STRAIN = "failure_strain"; // 破坏应变
        }

        // 压力老化容器试验
        public static class PAV {
            public static final String PRESSURE = "pressure"; // 压力
            public static final String DURATION = "duration"; // 时间
        }

        // 多重应力蠕变恢复试验
        public static class MSCR {
            public static final String FREQUENCY = "frequency"; // 加载频率
            public static final String INITIAL_STRAIN = "initial_strain"; // 初始应变
            public static final String CREEP_STRAIN = "creep_strain"; // 蠕变循环应变
            public static final String RECOVERY_STRAIN = "recovery_strain"; // 恢复循环应变
        }

        // 拉伸性能试验
        public static class ForceDuctility {
            public static final String STRETCH_SPEED = "stretch_speed"; // 拉伸速度
            public static final String MAX_FORCE = "max_force"; // 最大拉力值
            public static final String MAX_DEFORMATION = "max_deformation"; // 最大变形值
        }

        // 布鲁克菲尔德旋转黏度试验
        public static class BrookfieldViscosity {
            public static final String TEMPERATURE_PREFIX = "temperature_"; // 试验温度前缀
            public static final String SPINDLE_TYPE_PREFIX = "spindle_type_"; // 转子型号前缀
            public static final String ROTATION_SPEED_PREFIX = "rotation_speed_"; // 转速前缀
            public static final String VISCOSITY_PREFIX = "viscosity_"; // 黏度值前缀
            public static final String ROTOR_TYPE = "rotor_type"; // 转子型号
            public static final String ROTATION_SPEED = "rotation_speed"; // 转速
        }
    }
}
