package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 沥青混合料单轴压缩试验（圆柱体法）响应模型
 */
@Keep
public class UniaxialCompressionTestResponse implements Parcelable {
    @SerializedName("taskId")
    private String taskId;

    @SerializedName("testDate")
    private String testDate;

    @SerializedName("operator")
    private String operator;

    @SerializedName("testTemperature")
    private double testTemperature;

    @SerializedName("specimens")
    private List<SpecimenData> specimens;

    public UniaxialCompressionTestResponse() {
        // 必须提供无参构造函数用于GSON反序列化
    }

    protected UniaxialCompressionTestResponse(Parcel in) {
        taskId = in.readString();
        testDate = in.readString();
        operator = in.readString();
        testTemperature = in.readDouble();
        specimens = new ArrayList<>();
        in.readTypedList(specimens, SpecimenData.CREATOR);
    }

    public static final Creator<UniaxialCompressionTestResponse> CREATOR = new Creator<UniaxialCompressionTestResponse>() {
        @Override
        public UniaxialCompressionTestResponse createFromParcel(Parcel in) {
            return new UniaxialCompressionTestResponse(in);
        }

        @Override
        public UniaxialCompressionTestResponse[] newArray(int size) {
            return new UniaxialCompressionTestResponse[size];
        }
    };

    /**
     * 试件数据
     */
    @Keep
    public static class SpecimenData implements Parcelable {
        @SerializedName("specimenNumber")
        private int specimenNumber;

        @SerializedName("diameterMm")
        private double diameterMm;

        @SerializedName("heightMm")
        private double heightMm;

        @SerializedName("utmData")
        private Object utmDataRaw; 
        private List<UtmData> utmDataList = new ArrayList<>(); 

        @SerializedName("strengthData")
        private List<StrengthData> strengthData;

        @SerializedName("strengthAverage")
        private double strengthAverage;

        public SpecimenData() {
            // 必须提供无参构造函数用于GSON反序列化
            utmDataList = new ArrayList<>();
        }

        protected SpecimenData(Parcel in) {
            specimenNumber = in.readInt();
            diameterMm = in.readDouble();
            heightMm = in.readDouble();
            utmDataList = new ArrayList<>();
            in.readTypedList(utmDataList, UtmData.CREATOR);
            strengthData = new ArrayList<>();
            in.readTypedList(strengthData, StrengthData.CREATOR);
            strengthAverage = in.readDouble();
        }

        public static final Creator<SpecimenData> CREATOR = new Creator<SpecimenData>() {
            @Override
            public SpecimenData createFromParcel(Parcel in) {
                return new SpecimenData(in);
            }

            @Override
            public SpecimenData[] newArray(int size) {
                return new SpecimenData[size];
            }
        };

        // 处理从API返回的嵌套UTM数据结构
        public void processUtmData() {
            android.util.Log.d("SpecimenData", "开始处理UTM数据, utmDataRaw类型: " + 
                (utmDataRaw != null ? utmDataRaw.getClass().getName() : "null"));
            
            if (utmDataRaw == null) {
                android.util.Log.d("SpecimenData", "UTM数据为空");
                utmDataList = new ArrayList<>();
                return;
            }
            
            com.google.gson.Gson gson = new com.google.gson.Gson();
            
            try {
                // 将utmDataRaw转换为JSON字符串以便于调试
                String json = gson.toJson(utmDataRaw);
                android.util.Log.d("SpecimenData", "UTM数据JSON: " + json);
                
                // 处理嵌套结构 - 直接解析特定的JSON结构
                if (utmDataRaw instanceof com.google.gson.internal.LinkedTreeMap) {
                    com.google.gson.internal.LinkedTreeMap<String, Object> map = 
                        (com.google.gson.internal.LinkedTreeMap<String, Object>) utmDataRaw;
                    
                    // 检查是否存在utmDataList字段 - 这是后端返回的特殊嵌套结构
                    if (map.containsKey("utmDataList")) {
                        android.util.Log.d("SpecimenData", "发现嵌套的utmDataList字段");
                        Object nestedList = map.get("utmDataList");
                        
                        if (nestedList instanceof List) {
                            List<?> rawList = (List<?>) nestedList;
                            android.util.Log.d("SpecimenData", "原始嵌套列表大小: " + rawList.size());
                            
                            // 创建新的UTM数据列表
                            utmDataList = new ArrayList<>();
                            
                            // 逐个处理列表中的每个项目
                            for (Object item : rawList) {
                                String itemJson = gson.toJson(item);
                                try {
                                    UtmData utmData = gson.fromJson(itemJson, UtmData.class);
                                    // 确保压力级别存在
                                    if (utmData.getPressureLevel() == null || utmData.getPressureLevel().isEmpty()) {
                                        utmData.setPressureLevel("0.1P"); // 默认值
                                    }
                                    utmDataList.add(utmData);
                                    android.util.Log.d("SpecimenData", "成功解析嵌套列表项: 压力级别=" + 
                                            utmData.getPressureLevel() + ", 最大力=" + utmData.getMaxForceKn());
                                } catch (Exception e) {
                                    android.util.Log.e("SpecimenData", "解析列表项失败: " + e.getMessage());
                                }
                            }
                            
                            android.util.Log.d("SpecimenData", "成功解析嵌套列表，共 " + utmDataList.size() + " 项");
                            return;
                        } else {
                            android.util.Log.w("SpecimenData", "utmDataList字段不是List类型，而是: " + 
                                   (nestedList != null ? nestedList.getClass().getName() : "null"));
                        }
                    } else {
                        android.util.Log.d("SpecimenData", "未发现嵌套的utmDataList字段，尝试其他解析方法");
                        // 继续尝试其他解析方法
                    }
                }
                
                // 尝试标准的JSON解析
                if (utmDataRaw instanceof List) {
                    // 如果已经是列表，直接转换
                    utmDataList = (List<UtmData>) utmDataRaw;
                    android.util.Log.d("SpecimenData", "直接转换列表，大小: " + utmDataList.size());
                } else if (utmDataRaw instanceof com.google.gson.internal.LinkedTreeMap) {
                    // 尝试解析为单个UtmData对象
                    try {
                        UtmData utmData = gson.fromJson(json, UtmData.class);
                        // 确保有压力级别
                        if (utmData.getPressureLevel() == null || utmData.getPressureLevel().isEmpty()) {
                            utmData.setPressureLevel("0.1P");
                        }
                        utmDataList = new ArrayList<>();
                        utmDataList.add(utmData);
                        android.util.Log.d("SpecimenData", "解析为单个对象，压力级别: " + utmData.getPressureLevel());
                    } catch (Exception e) {
                        android.util.Log.e("SpecimenData", "无法解析为单个UtmData对象: " + e.getMessage());
                        utmDataList = new ArrayList<>();
                    }
                } else {
                    // 其他情况，尝试通用解析
                    try {
                        com.google.gson.reflect.TypeToken<List<UtmData>> typeToken = 
                            new com.google.gson.reflect.TypeToken<List<UtmData>>() {};
                        List<UtmData> parsedList = gson.fromJson(json, typeToken.getType());
                        
                        if (parsedList != null) {
                            utmDataList = parsedList;
                            android.util.Log.d("SpecimenData", "通用解析为列表，大小: " + utmDataList.size());
                        } else {
                            utmDataList = new ArrayList<>();
                            android.util.Log.d("SpecimenData", "通用解析结果为null");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SpecimenData", "通用解析失败: " + e.getMessage());
                        utmDataList = new ArrayList<>();
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("SpecimenData", "处理UTM数据时出错: " + e.getMessage());
                utmDataList = new ArrayList<>();
            }
            
            // 确保每个压力级别数据都有压力级别标识
            for (int i = 0; i < utmDataList.size(); i++) {
                UtmData data = utmDataList.get(i);
                if (data.getPressureLevel() == null || data.getPressureLevel().isEmpty()) {
                    double factor = (i + 1) / 10.0;
                    data.setPressureLevel(factor + "P");
                    android.util.Log.d("SpecimenData", "设置默认压力级别: " + data.getPressureLevel());
                }
            }
            
            android.util.Log.d("SpecimenData", "UTM数据处理完成，共 " + utmDataList.size() + " 条记录");
        }
        
        // UTM数据的getter和setter
        public List<UtmData> getUtmDataList() {
            if (utmDataList == null || utmDataList.isEmpty()) {
                processUtmData();
            }
            return utmDataList;
        }

        public void setUtmDataList(List<UtmData> utmDataList) {
            this.utmDataList = utmDataList;
        }
        
        // 添加单个UtmData的便捷方法
        public void addUtmData(UtmData utmData) {
            if (this.utmDataList == null) {
                this.utmDataList = new ArrayList<>();
            }
            this.utmDataList.add(utmData);
        }
        
        // 获取指定压力级别的UTM数据
        public UtmData getUtmDataByPressureLevel(String pressureLevel) {
            List<UtmData> utmList = getUtmDataList();
            for (UtmData data : utmList) {
                if (pressureLevel.equals(data.getPressureLevel())) {
                    return data;
                }
            }
            return null;
        }
        
        // 兼容旧版本的方法，获取第一个UTM数据或返回null
        public UtmData getUtmData() {
            List<UtmData> list = getUtmDataList();
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        }
        
        // 兼容旧版本的方法，设置单个UTM数据
        public void setUtmData(UtmData utmData) {
            utmDataList = new ArrayList<>();
            utmDataList.add(utmData);
            this.utmDataRaw = utmData;
        }

        @Keep
        public static class UtmData implements Parcelable {
            // 压力级别 (0.1P, 0.2P, 等)
            @SerializedName("pressureLevel")
            private String pressureLevel;

            @SerializedName("maxForceKn")
            private double maxForceKn;

            @SerializedName("minForceN")
            private double minForceN;

            @SerializedName("stressDevKpa")
            private double stressDevKpa;

            @SerializedName("displResilMm")
            private double displResilMm;

            @SerializedName("strainResil")
            private double strainResil;

            @SerializedName("resilientModulusMpa")
            private double resilientModulusMpa;

            @SerializedName("temperature")
            private double temperature;

            public UtmData() {
                // 必须提供无参构造函数用于GSON反序列化
            }

            protected UtmData(Parcel in) {
                pressureLevel = in.readString();
                maxForceKn = in.readDouble();
                minForceN = in.readDouble();
                stressDevKpa = in.readDouble();
                displResilMm = in.readDouble();
                strainResil = in.readDouble();
                resilientModulusMpa = in.readDouble();
                temperature = in.readDouble();
            }

            public static final Creator<UtmData> CREATOR = new Creator<UtmData>() {
                @Override
                public UtmData createFromParcel(Parcel in) {
                    return new UtmData(in);
                }

                @Override
                public UtmData[] newArray(int size) {
                    return new UtmData[size];
                }
            };

            // Getters and Setters
            public String getPressureLevel() {
                return pressureLevel;
            }

            public void setPressureLevel(String pressureLevel) {
                this.pressureLevel = pressureLevel;
            }

            public double getMaxForceKn() {
                return maxForceKn;
            }

            public void setMaxForceKn(double maxForceKn) {
                this.maxForceKn = maxForceKn;
            }

            public double getMinForceN() {
                return minForceN;
            }

            public void setMinForceN(double minForceN) {
                this.minForceN = minForceN;
            }

            public double getStressDevKpa() {
                return stressDevKpa;
            }

            public void setStressDevKpa(double stressDevKpa) {
                this.stressDevKpa = stressDevKpa;
            }

            public double getDisplResilMm() {
                return displResilMm;
            }

            public void setDisplResilMm(double displResilMm) {
                this.displResilMm = displResilMm;
            }

            public double getStrainResil() {
                return strainResil;
            }

            public void setStrainResil(double strainResil) {
                this.strainResil = strainResil;
            }

            public double getResilientModulusMpa() {
                return resilientModulusMpa;
            }

            public void setResilientModulusMpa(double resilientModulusMpa) {
                this.resilientModulusMpa = resilientModulusMpa;
            }

            public double getTemperature() {
                return temperature;
            }

            public void setTemperature(double temperature) {
                this.temperature = temperature;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(pressureLevel);
                dest.writeDouble(maxForceKn);
                dest.writeDouble(minForceN);
                dest.writeDouble(stressDevKpa);
                dest.writeDouble(displResilMm);
                dest.writeDouble(strainResil);
                dest.writeDouble(resilientModulusMpa);
                dest.writeDouble(temperature);
            }
        }

        @Keep
        public static class StrengthData implements Parcelable {
            @SerializedName("label")
            private String label;

            @SerializedName("valueKn")
            private double valueKn;

            public StrengthData() {
                // 必须提供无参构造函数用于GSON反序列化
            }

            protected StrengthData(Parcel in) {
                label = in.readString();
                valueKn = in.readDouble();
            }

            public static final Creator<StrengthData> CREATOR = new Creator<StrengthData>() {
                @Override
                public StrengthData createFromParcel(Parcel in) {
                    return new StrengthData(in);
                }

                @Override
                public StrengthData[] newArray(int size) {
                    return new StrengthData[size];
                }
            };

            // Getters and Setters
            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public double getValueKn() {
                return valueKn;
            }

            public void setValueKn(double valueKn) {
                this.valueKn = valueKn;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(label);
                dest.writeDouble(valueKn);
            }
        }

        // 初始化默认的强度数据
        public void initDefaultStrengthData() {
            if (this.strengthData == null) {
                this.strengthData = new ArrayList<>();
                
                // 如果有UTM数据但没有强度数据，创建一个默认的强度数据
                if (getUtmDataList() != null && !getUtmDataList().isEmpty()) {
                    StrengthData defaultStrength = new StrengthData();
                    defaultStrength.setLabel("P1");
                    // 使用第一个UTM数据的最大力值作为默认强度值
                    UtmData firstUtm = getUtmDataList().get(0);
                    if (firstUtm != null) {
                        defaultStrength.setValueKn(firstUtm.getMaxForceKn());
                    } else {
                        defaultStrength.setValueKn(0.0);
                    }
                    this.strengthData.add(defaultStrength);
                    
                    // 设置平均强度值与单个值相同
                    this.strengthAverage = defaultStrength.getValueKn();
                }
            }
        }
        
        public List<StrengthData> getStrengthData() {
            if (strengthData == null) {
                initDefaultStrengthData();
            }
            return strengthData;
        }

        public void setStrengthData(List<StrengthData> strengthData) {
            this.strengthData = strengthData;
        }

        public double getStrengthAverage() {
            return strengthAverage;
        }

        public void setStrengthAverage(double strengthAverage) {
            this.strengthAverage = strengthAverage;
        }

        // Getters and Setters
        public int getSpecimenNumber() {
            return specimenNumber;
        }

        public void setSpecimenNumber(int specimenNumber) {
            this.specimenNumber = specimenNumber;
        }

        public double getDiameterMm() {
            return diameterMm;
        }

        public void setDiameterMm(double diameterMm) {
            this.diameterMm = diameterMm;
        }

        public double getHeightMm() {
            return heightMm;
        }

        public void setHeightMm(double heightMm) {
            this.heightMm = heightMm;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(specimenNumber);
            dest.writeDouble(diameterMm);
            dest.writeDouble(heightMm);
            dest.writeTypedList(getUtmDataList());
            dest.writeTypedList(strengthData);
            dest.writeDouble(strengthAverage);
        }
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public double getTestTemperature() {
        return testTemperature;
    }

    public void setTestTemperature(double testTemperature) {
        this.testTemperature = testTemperature;
    }

    public List<SpecimenData> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(List<SpecimenData> specimens) {
        this.specimens = specimens;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(testDate);
        dest.writeString(operator);
        dest.writeDouble(testTemperature);
        dest.writeTypedList(specimens);
    }
}
