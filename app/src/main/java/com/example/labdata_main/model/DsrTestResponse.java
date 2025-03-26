package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 动态剪切流变仪(DSR)实验响应数据模型类
 */
public class DsrTestResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("taskId")
    private String taskId;

    @SerializedName("specimenId")
    private String specimenId;

    @SerializedName("specimenType")
    private String specimenType;

    @SerializedName("materialType")
    private String materialType;

    @SerializedName("controlMode")
    private String controlMode;

    @SerializedName("testRadius")
    private Double testRadius;

    @SerializedName("plateGap")
    private Double plateGap;

    @SerializedName("experimentValues")
    private String experimentValues;

    @SerializedName("remarks")
    private String remarks;
    
    // 新API结构直接返回的数据点列表
    @SerializedName("dataPoints")
    private List<DataPoint> dataPoints;

    // 内部类，用于解析experimentValues字段中的实验数据点
    public static class DataPoint {
        @SerializedName("temperature")
        private Double temperature;
        
        @SerializedName("frequency")
        private Double frequency;
        
        @SerializedName("complexModulus")
        private Double complexModulus;  // 复数模量G*
        
        @SerializedName("maxShearStress")
        private Double maxShearStress;  // 最大剪切应力
        
        @SerializedName("maxShearStrain")
        private Double maxShearStrain;  // 最大剪切应变
        
        @SerializedName("phaseAngle")
        private Double phaseAngle;      // 相位角δ
        
        private Double storageModulus;  // 储能模量G'
        private Double lossModulus;     // 损耗模量G"
        private String viscoelasticGrade; // 黏弹性等级

        public DataPoint() {
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getFrequency() {
            return frequency;
        }

        public void setFrequency(Double frequency) {
            this.frequency = frequency;
        }

        public Double getComplexModulus() {
            return complexModulus;
        }

        public void setComplexModulus(Double complexModulus) {
            this.complexModulus = complexModulus;
        }
        
        public Double getMaxShearStress() {
            return maxShearStress;
        }

        public void setMaxShearStress(Double maxShearStress) {
            this.maxShearStress = maxShearStress;
        }

        public Double getMaxShearStrain() {
            return maxShearStrain;
        }

        public void setMaxShearStrain(Double maxShearStrain) {
            this.maxShearStrain = maxShearStrain;
        }

        public Double getStorageModulus() {
            // 如果储能模量未设置，但是有复合模量和相位角，则计算
            if (storageModulus == null && complexModulus != null && phaseAngle != null) {
                storageModulus = complexModulus * Math.cos(Math.toRadians(phaseAngle));
            }
            return storageModulus;
        }

        public void setStorageModulus(Double storageModulus) {
            this.storageModulus = storageModulus;
        }

        public Double getLossModulus() {
            // 如果损耗模量未设置，但是有复合模量和相位角，则计算
            if (lossModulus == null && complexModulus != null && phaseAngle != null) {
                lossModulus = complexModulus * Math.sin(Math.toRadians(phaseAngle));
            }
            return lossModulus;
        }

        public void setLossModulus(Double lossModulus) {
            this.lossModulus = lossModulus;
        }

        public Double getPhaseAngle() {
            return phaseAngle;
        }

        public void setPhaseAngle(Double phaseAngle) {
            this.phaseAngle = phaseAngle;
        }

        public String getViscoelasticGrade() {
            return viscoelasticGrade;
        }

        public void setViscoelasticGrade(String viscoelasticGrade) {
            this.viscoelasticGrade = viscoelasticGrade;
        }
    }

    // 获取数据点列表，优先使用直接返回的dataPoints，如果为空则尝试解析experimentValues
    public List<DataPoint> getDataPoints() {
        // 如果dataPoints已设置，直接返回
        if (dataPoints != null && !dataPoints.isEmpty()) {
            return dataPoints;
        }
        
        // 否则尝试解析experimentValues字段（兼容旧版API）
        if (experimentValues == null || experimentValues.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> dataList = gson.fromJson(experimentValues, type);

            List<DataPoint> points = new ArrayList<>();
            for (Map<String, Object> data : dataList) {
                DataPoint point = new DataPoint();

                if (data.containsKey("temperature")) {
                    point.setTemperature(Double.parseDouble(data.get("temperature").toString()));
                }
                if (data.containsKey("frequency")) {
                    point.setFrequency(Double.parseDouble(data.get("frequency").toString()));
                }
                if (data.containsKey("complexModulus")) {
                    point.setComplexModulus(Double.parseDouble(data.get("complexModulus").toString()));
                }
                if (data.containsKey("phaseAngle")) {
                    point.setPhaseAngle(Double.parseDouble(data.get("phaseAngle").toString()));
                }
                
                points.add(point);
            }
            return points;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }

    public String getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(String specimenType) {
        this.specimenType = specimenType;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getControlMode() {
        return controlMode;
    }

    public void setControlMode(String controlMode) {
        this.controlMode = controlMode;
    }

    public Double getTestRadius() {
        return testRadius;
    }

    public void setTestRadius(Double testRadius) {
        this.testRadius = testRadius;
    }

    public Double getPlateGap() {
        return plateGap;
    }

    public void setPlateGap(Double plateGap) {
        this.plateGap = plateGap;
    }

    public String getExperimentValues() {
        return experimentValues;
    }

    public void setExperimentValues(String experimentValues) {
        this.experimentValues = experimentValues;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
