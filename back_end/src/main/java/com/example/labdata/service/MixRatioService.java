package com.example.labdata.service;

import com.example.labdata.model.*;
import com.example.labdata.payload.request.MixRatioRequest;
import com.example.labdata.payload.response.MixRatioResponse;
import com.example.labdata.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MixRatioService {

    private final MixRatioRepository mixRatioRepository;
    private final MixRatioAsphaltRepository mixRatioAsphaltRepository;
    private final MixRatioSandRepository mixRatioSandRepository;
    private final MixRatioStoneRepository mixRatioStoneRepository;
    private final AsphaltMaterialRepository asphaltMaterialRepository;
    private final SandMaterialRepository sandMaterialRepository;
    private final StoneMaterialRepository stoneMaterialRepository;

    @Autowired
    public MixRatioService(
            MixRatioRepository mixRatioRepository,
            MixRatioAsphaltRepository mixRatioAsphaltRepository,
            MixRatioSandRepository mixRatioSandRepository,
            MixRatioStoneRepository mixRatioStoneRepository,
            AsphaltMaterialRepository asphaltMaterialRepository,
            SandMaterialRepository sandMaterialRepository,
            StoneMaterialRepository stoneMaterialRepository) {
        this.mixRatioRepository = mixRatioRepository;
        this.mixRatioAsphaltRepository = mixRatioAsphaltRepository;
        this.mixRatioSandRepository = mixRatioSandRepository;
        this.mixRatioStoneRepository = mixRatioStoneRepository;
        this.asphaltMaterialRepository = asphaltMaterialRepository;
        this.sandMaterialRepository = sandMaterialRepository;
        this.stoneMaterialRepository = stoneMaterialRepository;
    }

    public List<MixRatioResponse> getAllMixRatios() {
        List<MixRatio> mixRatios = mixRatioRepository.findAll();
        return mixRatios.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取综合配比数据，包含mixratio，mixratio_asphalt，mixratio_sand，mixratio_stone表的数据
     * 用于前端展示完整的配比信息
     * @return 包含完整配比信息的响应列表
     */
    public List<MixRatioResponse> getComprehensiveMixRatios() {
        // 获取所有配比信息，并转换为完整响应
        List<MixRatio> mixRatios = mixRatioRepository.findAll();
        
        // 使用现有的转换方法，convertToDetailedResponse已经包含了获取相关表数据的逻辑
        return mixRatios.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());
    }

    public MixRatioResponse getMixRatioById(Long id) {
        MixRatio mixRatio = findMixRatioById(id);
        return convertToDetailedResponse(mixRatio);
    }

    public MixRatioResponse getMixRatioByMixId(String mixId) {
        MixRatio mixRatio = mixRatioRepository.findByMixId(mixId)
                .orElseThrow(() -> new EntityNotFoundException("配合比未找到，ID: " + mixId));
        return convertToDetailedResponse(mixRatio);
    }

    @Transactional
    public MixRatioResponse createMixRatio(MixRatioRequest request) {
        // 创建主要的配合比记录
        MixRatio mixRatio = new MixRatio();
        mixRatio.setMixName(request.getMixName());
        
        // 设置用户单位ID和用户ID
        if (request.getMixCompany() != null) {
            mixRatio.setMixCompany(String.valueOf(request.getMixCompany()));
        }
        mixRatio.setCreatedBy(request.getCreatedBy());
        
        // 生成配合比ID: phb + 年月日 + 序号（如 phb20250314002）
        mixRatio.setMixId(generateMixRatioId());
        
        MixRatio savedMixRatio = mixRatioRepository.save(mixRatio);
        
        // 添加沥青材料
        List<MixRatioAsphalt> asphaltComponents = new ArrayList<>();
        for (MixRatioRequest.AsphaltComponentRequest component : request.getAsphaltComponents()) {
            AsphaltMaterial asphaltMaterial = asphaltMaterialRepository.findById(component.getAsphaltId())
                    .orElseThrow(() -> new EntityNotFoundException("沥青材料未找到，ID: " + component.getAsphaltId()));
            
            MixRatioAsphalt mixRatioAsphalt = new MixRatioAsphalt();
            mixRatioAsphalt.setMixRatio(savedMixRatio);
            mixRatioAsphalt.setAsphaltMaterial(asphaltMaterial);
            mixRatioAsphalt.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            asphaltComponents.add(mixRatioAsphaltRepository.save(mixRatioAsphalt));
        }
        
        // 添加沙子材料
        List<MixRatioSand> sandComponents = new ArrayList<>();
        for (MixRatioRequest.SandComponentRequest component : request.getSandComponents()) {
            SandMaterial sandMaterial = sandMaterialRepository.findById(component.getSandId())
                    .orElseThrow(() -> new EntityNotFoundException("沙子材料未找到，ID: " + component.getSandId()));
            
            MixRatioSand mixRatioSand = new MixRatioSand();
            mixRatioSand.setMixRatio(savedMixRatio);
            mixRatioSand.setSandMaterial(sandMaterial);
            mixRatioSand.setGradation(component.getGradation());
            mixRatioSand.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            sandComponents.add(mixRatioSandRepository.save(mixRatioSand));
        }
        
        // 添加石子材料
        List<MixRatioStone> stoneComponents = new ArrayList<>();
        for (MixRatioRequest.StoneComponentRequest component : request.getStoneComponents()) {
            StoneMaterial stoneMaterial = stoneMaterialRepository.findById(component.getStoneId())
                    .orElseThrow(() -> new EntityNotFoundException("石子材料未找到，ID: " + component.getStoneId()));
            
            MixRatioStone mixRatioStone = new MixRatioStone();
            mixRatioStone.setMixRatio(savedMixRatio);
            mixRatioStone.setStoneMaterial(stoneMaterial);
            mixRatioStone.setGradation(component.getGradation());
            mixRatioStone.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            stoneComponents.add(mixRatioStoneRepository.save(mixRatioStone));
        }
        
        // 转换为响应对象
        MixRatioResponse response = new MixRatioResponse(savedMixRatio);
        
        response.setAsphaltComponents(asphaltComponents.stream()
                .map(this::convertToAsphaltComponentResponse)
                .collect(Collectors.toList()));
        
        response.setSandComponents(sandComponents.stream()
                .map(this::convertToSandComponentResponse)
                .collect(Collectors.toList()));
        
        response.setStoneComponents(stoneComponents.stream()
                .map(this::convertToStoneComponentResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    @Transactional
    public MixRatioResponse updateMixRatio(Long id, MixRatioRequest request) {
        MixRatio mixRatio = findMixRatioById(id);
        mixRatio.setMixName(request.getMixName());
        
        // 更新用户单位ID和用户ID（如果请求中包含这些字段）
        if (request.getMixCompany() != null) {
            mixRatio.setMixCompany(String.valueOf(request.getMixCompany()));
        }
        
        if (request.getCreatedBy() != null) {
            mixRatio.setCreatedBy(request.getCreatedBy());
        }
        
        mixRatio = mixRatioRepository.save(mixRatio);
        
        // 删除现有组件
        mixRatioAsphaltRepository.deleteByMixRatio(mixRatio);
        mixRatioSandRepository.deleteByMixRatio(mixRatio);
        mixRatioStoneRepository.deleteByMixRatio(mixRatio);
        
        // 添加新的沥青材料
        List<MixRatioAsphalt> asphaltComponents = new ArrayList<>();
        for (MixRatioRequest.AsphaltComponentRequest component : request.getAsphaltComponents()) {
            AsphaltMaterial asphaltMaterial = asphaltMaterialRepository.findById(component.getAsphaltId())
                    .orElseThrow(() -> new EntityNotFoundException("沥青材料未找到，ID: " + component.getAsphaltId()));
            
            MixRatioAsphalt mixRatioAsphalt = new MixRatioAsphalt();
            mixRatioAsphalt.setMixRatio(mixRatio);
            mixRatioAsphalt.setAsphaltMaterial(asphaltMaterial);
            mixRatioAsphalt.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            asphaltComponents.add(mixRatioAsphaltRepository.save(mixRatioAsphalt));
        }
        
        // 添加新的沙子材料
        List<MixRatioSand> sandComponents = new ArrayList<>();
        for (MixRatioRequest.SandComponentRequest component : request.getSandComponents()) {
            SandMaterial sandMaterial = sandMaterialRepository.findById(component.getSandId())
                    .orElseThrow(() -> new EntityNotFoundException("沙子材料未找到，ID: " + component.getSandId()));
            
            MixRatioSand mixRatioSand = new MixRatioSand();
            mixRatioSand.setMixRatio(mixRatio);
            mixRatioSand.setSandMaterial(sandMaterial);
            mixRatioSand.setGradation(component.getGradation());
            mixRatioSand.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            sandComponents.add(mixRatioSandRepository.save(mixRatioSand));
        }
        
        // 添加新的石子材料
        List<MixRatioStone> stoneComponents = new ArrayList<>();
        for (MixRatioRequest.StoneComponentRequest component : request.getStoneComponents()) {
            StoneMaterial stoneMaterial = stoneMaterialRepository.findById(component.getStoneId())
                    .orElseThrow(() -> new EntityNotFoundException("石子材料未找到，ID: " + component.getStoneId()));
            
            MixRatioStone mixRatioStone = new MixRatioStone();
            mixRatioStone.setMixRatio(mixRatio);
            mixRatioStone.setStoneMaterial(stoneMaterial);
            mixRatioStone.setGradation(component.getGradation());
            mixRatioStone.setPercentage(BigDecimal.valueOf(component.getPercentage()));
            
            stoneComponents.add(mixRatioStoneRepository.save(mixRatioStone));
        }
        
        // 转换为响应对象
        MixRatioResponse response = new MixRatioResponse(mixRatio);
        
        response.setAsphaltComponents(asphaltComponents.stream()
                .map(this::convertToAsphaltComponentResponse)
                .collect(Collectors.toList()));
        
        response.setSandComponents(sandComponents.stream()
                .map(this::convertToSandComponentResponse)
                .collect(Collectors.toList()));
        
        response.setStoneComponents(stoneComponents.stream()
                .map(this::convertToStoneComponentResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    @Transactional
    public void deleteMixRatio(Long id) {
        MixRatio mixRatio = findMixRatioById(id);
        
        // 由于使用了级联删除，这些代码实际上不需要，但出于清晰性添加
        mixRatioAsphaltRepository.deleteByMixRatio(mixRatio);
        mixRatioSandRepository.deleteByMixRatio(mixRatio);
        mixRatioStoneRepository.deleteByMixRatio(mixRatio);
        
        mixRatioRepository.delete(mixRatio);
    }

    // 辅助方法
    private MixRatio findMixRatioById(Long id) {
        return mixRatioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("配合比未找到，ID: " + id));
    }

    private MixRatioResponse convertToDetailedResponse(MixRatio mixRatio) {
        MixRatioResponse response = new MixRatioResponse(mixRatio);
        
        // 加载沥青组件
        List<MixRatioAsphalt> asphaltComponents = mixRatioAsphaltRepository.findByMixRatio(mixRatio);
        response.setAsphaltComponents(asphaltComponents.stream()
                .map(this::convertToAsphaltComponentResponse)
                .collect(Collectors.toList()));
        
        // 加载沙子组件
        List<MixRatioSand> sandComponents = mixRatioSandRepository.findByMixRatio(mixRatio);
        response.setSandComponents(sandComponents.stream()
                .map(this::convertToSandComponentResponse)
                .collect(Collectors.toList()));
        
        // 加载石子组件
        List<MixRatioStone> stoneComponents = mixRatioStoneRepository.findByMixRatio(mixRatio);
        response.setStoneComponents(stoneComponents.stream()
                .map(this::convertToStoneComponentResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    private MixRatioResponse.AsphaltComponentResponse convertToAsphaltComponentResponse(MixRatioAsphalt component) {
        return new MixRatioResponse.AsphaltComponentResponse(
                component.getId(),
                component.getAsphaltMaterial().getId(),
                component.getAsphaltMaterial().getName(),
                component.getAsphaltMaterial().getGrade(),
                component.getAsphaltMaterial().getCharacter(),
                component.getPercentage().doubleValue()
        );
    }

    private MixRatioResponse.SandComponentResponse convertToSandComponentResponse(MixRatioSand component) {
        return new MixRatioResponse.SandComponentResponse(
                component.getId(),
                component.getSandMaterial().getId(),
                component.getSandMaterial().getName(),
                component.getGradation(),
                component.getPercentage().doubleValue()
        );
    }

    private MixRatioResponse.StoneComponentResponse convertToStoneComponentResponse(MixRatioStone component) {
        return new MixRatioResponse.StoneComponentResponse(
                component.getId(),
                component.getStoneMaterial().getId(),
                component.getStoneMaterial().getName(),
                component.getGradation(),
                component.getPercentage().doubleValue()
        );
    }

    /**
     * 生成配合比唯一ID
     * 格式：phb + 年月日 + 序号（如 phb20250314002）
     */
    private String generateMixRatioId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePrefix = "phb" + LocalDate.now().format(formatter);
        
        // 获取当天开始和结束时间
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        // 计算今天已有的记录数
        int sequenceNumber = mixRatioRepository.countMixRatiosCreatedBetween(startOfDay, endOfDay) + 1;
        
        // 格式化序号为3位数字（例如：001, 002, ...）
        String sequenceFormatted = String.format("%03d", sequenceNumber);
        
        return datePrefix + sequenceFormatted;
    }
    
    /**
     * 根据公司ID获取该公司下的所有配比
     * 
     * @param companyId 公司ID (可以是数字或字符串)
     * @return 该公司的配比列表，包含详细信息
     */
    public List<MixRatioResponse> getMixRatiosByCompany(String companyId) {
        // 直接使用字符串类型的公司ID查询
        List<MixRatio> mixRatios = mixRatioRepository.findByMixCompany(companyId);
        
        // 如果没有找到结果，尝试使用字符串匹配方法
        if (mixRatios.isEmpty()) {
            mixRatios = mixRatioRepository.findByMixCompanyString(companyId);
        }
        
        return mixRatios.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());
    }
}
