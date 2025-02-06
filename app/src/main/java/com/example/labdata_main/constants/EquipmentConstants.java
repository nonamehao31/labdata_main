package com.example.labdata_main.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentConstants {
    // 设备类型
    public static final String TYPE_MIXING = "MIXING";      // 拌合设备
    public static final String TYPE_FORMING = "FORMING";    // 制件设备
    public static final String TYPE_TESTING = "TESTING";    // 实验设备

    // 厂家信息
    public static final String MANUFACTURER_INFRATEST = "infratest";
    public static final String MANUFACTURER_CONTROLS = "Controls";
    public static final String MANUFACTURER_JILISEN = "浙江吉力森";
    public static final String MANUFACTURER_TANKUANG = "浙江探矿";

    // 设备信息映射
    private static final Map<String, Map<String, List<String>>> EQUIPMENT_INFO = new HashMap<>();

    static {
        // 拌合设备
        Map<String, List<String>> mixingEquipment = new HashMap<>();
        mixingEquipment.put(MANUFACTURER_INFRATEST, Arrays.asList("20-0160-60"));
        mixingEquipment.put(MANUFACTURER_CONTROLS, Arrays.asList("77-PV0077/C"));
        EQUIPMENT_INFO.put(TYPE_MIXING, mixingEquipment);

        // 制件设备
        Map<String, List<String>> formingEquipment = new HashMap<>();
        formingEquipment.put(MANUFACTURER_INFRATEST, Arrays.asList("20-1500", "60-0220")); // 马歇尔击实仪, 制样切割机
        formingEquipment.put(MANUFACTURER_CONTROLS, Arrays.asList("77-PV41A02", "77-PV75202")); // 马歇尔击实仪, 制样切割机
        EQUIPMENT_INFO.put(TYPE_FORMING, formingEquipment);

        // 实验设备
        Map<String, List<String>> testingEquipment = new HashMap<>();
        testingEquipment.put(MANUFACTURER_INFRATEST, Arrays.asList("20-1672")); // 马歇尔稳定度仪
        testingEquipment.put(MANUFACTURER_CONTROLS, Arrays.asList("76-B3002")); // 马歇尔稳定度仪
        EQUIPMENT_INFO.put(TYPE_TESTING, testingEquipment);

        // 筛分设备
        Map<String, List<String>> sievingEquipment = new HashMap<>();
        sievingEquipment.put(MANUFACTURER_JILISEN, Arrays.asList("ZBSX-92A")); // 集料筛分仪
        sievingEquipment.put(MANUFACTURER_TANKUANG, Arrays.asList("8411")); // 集料筛分仪
        EQUIPMENT_INFO.put("SIEVING", sievingEquipment);
    }

    /**
     * 获取指定类型的所有厂家
     */
    public static List<String> getManufacturers(String type) {
        Map<String, List<String>> manufacturerMap = EQUIPMENT_INFO.get(type);
        if (manufacturerMap == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(manufacturerMap.keySet());
    }

    /**
     * 获取指定类型和厂家的所有型号
     */
    public static List<String> getModels(String type, String manufacturer) {
        Map<String, List<String>> manufacturerMap = EQUIPMENT_INFO.get(type);
        if (manufacturerMap == null) {
            return new ArrayList<>();
        }
        List<String> models = manufacturerMap.get(manufacturer);
        return models != null ? new ArrayList<>(models) : new ArrayList<>();
    }

    /**
     * 获取设备类型的显示名称
     */
    public static String getTypeDisplayName(String type) {
        switch (type) {
            case TYPE_MIXING:
                return "拌合设备";
            case TYPE_FORMING:
                return "制件设备";
            case TYPE_TESTING:
                return "实验设备";
            case "SIEVING":
                return "筛分设备";
            default:
                return "未知设备";
        }
    }
}
