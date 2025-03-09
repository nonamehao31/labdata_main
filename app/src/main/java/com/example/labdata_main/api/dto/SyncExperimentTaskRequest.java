package com.example.labdata_main.api.dto;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.LabDataApplication;
import com.example.labdata_main.dao.MaterialDao;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实验任务同步请求DTO
 * 用于将本地实验任务数据转换为与后端兼容的格式
 */
public class SyncExperimentTaskRequest {
    // 基本信息
    private Long id;
    private String name; // 与后端一致的字段名
    private String description; // 与后端一致的字段名
    private Long organizationId; // 组织ID，用于多单位隔离
    
    // 完整对象数据，而不仅仅是ID引用
    private ProjectDto project;
    private MixingMethodDto mixingMethod;
    private MixRatioDto mixRatio;
    private List<MaterialDto> materials = new ArrayList<>();
    
    // 状态信息
    private String status;
    private Long scheduledStartTime;
    private Long scheduledEndTime;
    private Long actualStartTime;
    private Long actualEndTime;
    
    // 同步信息
    private Long clientId;
    
    /**
     * 从本地实验任务创建同步请求对象
     */
    public static SyncExperimentTaskRequest fromExperimentTask(ExperimentTask task, Long organizationId) {
        SyncExperimentTaskRequest request = new SyncExperimentTaskRequest();
        
        // 设置基本信息
        request.id = task.getId() > 0 ? task.getId() : null;
        request.name = task.getTaskName();
        request.description = task.getNotes();
        request.organizationId = organizationId; // 设置组织ID，用于数据隔离
        
        // 设置项目信息 - 创建完整的项目数据传输对象
        if (task.getProjectId() > 0) {
            ProjectDto projectDto = new ProjectDto();
            projectDto.setId(task.getProjectId());
            projectDto.setName(task.getProjectName() != null ? task.getProjectName() : "项目 " + task.getProjectId());
            projectDto.setOrganizationId(organizationId);
            // 可以设置更多项目相关字段
            request.project = projectDto;
        }
        
        // 设置混合方法信息 - 创建完整的混合方法数据传输对象
        if (task.getMixingMethodId() != null && task.getMixingMethodId() > 0) {
            MixingMethodDto methodDto = new MixingMethodDto();
            methodDto.setId(task.getMixingMethodId());
            methodDto.setName(task.getMoldingMethod() != null ? task.getMoldingMethod() : "制件方法 " + task.getMixingMethodId());
            methodDto.setOrganizationId(organizationId);
            request.mixingMethod = methodDto;
        }
        
        // 设置配比信息 - 创建完整的配比数据传输对象
        if (task.getMixRatioId() != null && task.getMixRatioId() > 0 && !task.getSelectedMixRatios().isEmpty()) {
            MixRatioDto mixRatioDto = new MixRatioDto();
            mixRatioDto.setId(task.getMixRatioId());
            mixRatioDto.setOrganizationId(organizationId);
            
            // 如果本地有配比详情，则设置相关信息
            if (!task.getSelectedMixRatios().isEmpty()) {
                MixRatio localMixRatio = task.getSelectedMixRatios().get(0);
                mixRatioDto.setName(localMixRatio.getName() != null ? localMixRatio.getName() : "配比 " + task.getMixRatioId());
                mixRatioDto.setDescription(localMixRatio.getDescription());
                
                // 如果有配比材料信息，添加到DTO
                if (localMixRatio.getMaterials() != null && !localMixRatio.getMaterials().isEmpty()) {
                    List<MaterialDto> mixRatioMaterials = new ArrayList<>();
                    
                    for (MaterialItem materialItem : localMixRatio.getMaterials()) {
                        // 尝试获取材料详细信息
                        Material material = findMaterialById(materialItem.getId());
                        
                        if (material != null) {
                            MaterialDto materialDto = new MaterialDto();
                            try {
                                materialDto.setId(Long.parseLong(material.getId()));
                            } catch (NumberFormatException e) {
                                // 如果不是Long类型的ID，则不设置ID
                            }
                            materialDto.setName(material.getName());
                            materialDto.setType(material.getCategory());
                            materialDto.setOrganizationId(organizationId);
                            mixRatioMaterials.add(materialDto);
                        } else {
                            // 如果找不到材料信息，使用MaterialItem的信息
                            MaterialDto materialDto = new MaterialDto();
                            materialDto.setId(materialItem.getId());
                            materialDto.setName(materialItem.getName() != null ? materialItem.getName() : "材料 " + materialItem.getId());
                            materialDto.setType("其他"); // 设置一个默认类型
                            materialDto.setOrganizationId(organizationId);
                            mixRatioMaterials.add(materialDto);
                        }
                    }
                    
                    if (!mixRatioMaterials.isEmpty()) {
                        mixRatioDto.setMaterials(mixRatioMaterials);
                    }
                }
            } else {
                // 如果没有配比详情，则设置一个默认名称
                mixRatioDto.setName("配比 " + task.getMixRatioId());
            }
            
            request.mixRatio = mixRatioDto;
        }
        
        // 设置原料信息 - 创建完整的原料数据传输对象列表
        if (task.getMaterialIds() != null && !task.getMaterialIds().isEmpty()) {
            for (Long materialId : task.getMaterialIds()) {
                if (materialId != null && materialId > 0) {
                    MaterialDto materialDto = new MaterialDto();
                    materialDto.setId(materialId);
                    // 通过本地数据库查询获取材料名称和其他信息
                    Material material = findMaterialById(materialId);
                    if (material != null) {
                        materialDto.setName(material.getName());
                        materialDto.setType(material.getCategory()); // 使用category作为type
                        materialDto.setDescription(material.getDescription());
                        materialDto.setOrganizationId(organizationId);
                    } else {
                        // 如果找不到材料信息，至少设置一个默认名称防止验证失败
                        materialDto.setName("材料 " + materialId);
                        materialDto.setType("unknown");
                        materialDto.setOrganizationId(organizationId);
                    }
                    request.materials.add(materialDto);
                }
            }
        }
        
        // 设置时间和状态
        request.scheduledStartTime = task.getDeadline();
        request.status = mapStatus(task.getStatus());
        request.clientId = task.getId() > 0 ? task.getId() : null; // 使用本地ID作为客户端ID
        
        return request;
    }
    
    /**
     * 将本地状态映射为后端状态
     */
    private static String mapStatus(String localStatus) {
        if (localStatus == null) return "SCHEDULED";
        
        switch (localStatus) {
            case "已接受":
                return "IN_PROGRESS";
            case "已完成":
                return "COMPLETED";
            case "已取消":
                return "CANCELLED";
            case "未接受":
            default:
                return "SCHEDULED";
        }
    }

    /**
     * 辅助方法：根据ID从本地数据库查找材料
     * @param materialId 材料ID
     * @return 材料对象，如果不存在则返回null
     */
    private static Material findMaterialById(Long materialId) {
        try {
            // 获取应用上下文
            Context context = LabDataApplication.getAppContext();
            if (context == null) {
                Log.e("SyncExperimentTask", "无法获取应用上下文");
                return null;
            }
            
            // 获取MaterialDao实例
            MaterialDao materialDao = AppDatabase.getInstance(context).materialDao();
            
            // 将Long类型的ID转换为String类型（因为本地数据库使用String类型的ID）
            String stringId = String.valueOf(materialId);
            
            // 尝试通过ID查询本地数据库
            Material material = materialDao.getMaterialById(stringId);
            return material;
        } catch (Exception e) {
            Log.e("SyncExperimentTask", "查询材料信息时出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 辅助方法：根据名称从本地数据库查找材料
     * @param materialName 材料名称
     * @return 材料对象，如果不存在则返回null
     */
    private static Material findMaterialByName(String materialName) {
        try {
            // 获取应用上下文
            Context context = LabDataApplication.getAppContext();
            if (context == null) {
                Log.e("SyncExperimentTask", "无法获取应用上下文");
                return null;
            }
            
            // 获取MaterialDao实例
            MaterialDao materialDao = AppDatabase.getInstance(context).materialDao();
            
            // 尝试通过名称查询本地数据库
            Material material = materialDao.getMaterialByName(materialName);
            return material;
        } catch (Exception e) {
            Log.e("SyncExperimentTask", "查询材料信息时出错: " + e.getMessage());
            return null;
        }
    }

    // 内部DTO类，用于项目数据传输
    public static class ProjectDto {
        private Long id;
        private String name;
        private String description;
        private Long organizationId; // 组织ID，用于多单位隔离
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }
    
    // 内部DTO类，用于混合方法数据传输
    public static class MixingMethodDto {
        private Long id;
        private String name;
        private String description;
        private Long organizationId; // 组织ID，用于多单位隔离
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }
    
    // 内部DTO类，用于配比数据传输
    public static class MixRatioDto {
        private Long id;
        private String name;
        private String description;
        private List<MaterialDto> materials = new ArrayList<>();
        private Long organizationId; // 组织ID，用于多单位隔离
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<MaterialDto> getMaterials() { return materials; }
        public void setMaterials(List<MaterialDto> materials) { this.materials = materials; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }
    
    // 内部DTO类，用于原料数据传输
    public static class MaterialDto {
        private Long id;
        private String name;
        private String description;
        private String type;
        private Long organizationId; // 组织ID，用于多单位隔离
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }

    // Getters and setters for the main class
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectDto getProject() {
        return project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }

    public MixingMethodDto getMixingMethod() {
        return mixingMethod;
    }

    public void setMixingMethod(MixingMethodDto mixingMethod) {
        this.mixingMethod = mixingMethod;
    }

    public MixRatioDto getMixRatio() {
        return mixRatio;
    }

    public void setMixRatio(MixRatioDto mixRatio) {
        this.mixRatio = mixRatio;
    }

    public List<MaterialDto> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(Long scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public Long getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(Long scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public Long getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Long actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public Long getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Long actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
