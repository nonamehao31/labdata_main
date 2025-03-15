package com.example.labdata.service;

import com.example.labdata.model.MixRatio;
import com.example.labdata.model.Specimen;
import com.example.labdata.payload.request.SpecimenRequest;
import com.example.labdata.payload.response.SpecimenResponse;
import com.example.labdata.repository.MixRatioRepository;
import com.example.labdata.repository.SpecimenRepository;
import com.example.labdata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecimenService {

    private final SpecimenRepository specimenRepository;
    private final MixRatioRepository mixRatioRepository;
    private final UserRepository userRepository;

    @Autowired
    public SpecimenService(SpecimenRepository specimenRepository, MixRatioRepository mixRatioRepository, UserRepository userRepository) {
        this.specimenRepository = specimenRepository;
        this.mixRatioRepository = mixRatioRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取特定配比的所有试件
     * @param mixRatioId 配比ID
     * @return 试件响应列表
     */
    public List<SpecimenResponse> getSpecimensByMixRatioId(Long mixRatioId) {
        List<Specimen> specimens = specimenRepository.findByMixRatioId(mixRatioId);
        return specimens.stream()
                .map(this::convertToSpecimenResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取试件
     * @param id 试件ID
     * @return 试件响应
     */
    public SpecimenResponse getSpecimenById(Long id) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试件不存在，ID: " + id));
        return convertToSpecimenResponse(specimen);
    }

    /**
     * 创建新试件
     * @param specimenRequest 试件请求数据
     * @return 创建的试件响应
     */
    public SpecimenResponse createSpecimen(SpecimenRequest specimenRequest) {
        try {
            // 验证配比是否存在（如果mixRatioId不为null）
            if (specimenRequest.getMixRatioId() != null && 
                !mixRatioRepository.existsById(specimenRequest.getMixRatioId())) {
                throw new RuntimeException("配比不存在，ID: " + specimenRequest.getMixRatioId());
            }

            // 优先使用请求中的createdBy，如果为null才获取当前用户ID
            Long userId;
            if (specimenRequest.getCreatedBy() != null) {
                userId = specimenRequest.getCreatedBy();
                // 验证用户是否存在
                if (!userRepository.existsById(userId)) {
                    // 如果指定用户不存在，记录警告并使用当前用户ID
                    System.out.println("警告: 请求中指定的用户ID (" + userId + ") 在数据库中不存在，尝试使用当前用户ID");
                    userId = getCurrentUserId();
                }
            } else {
                userId = getCurrentUserId();
            }
            
            Specimen specimen = new Specimen();
            specimen.setMixRatioId(specimenRequest.getMixRatioId());
            specimen.setMixingTemperature(specimenRequest.getMixingTemperature());
            specimen.setMixingSpeed(specimenRequest.getMixingSpeed());
            specimen.setMixingTime(specimenRequest.getMixingTime());
            specimen.setCompactionMethod(specimenRequest.getCompactionMethod());
            
            // 使用请求中的创建时间（如果有），否则使用当前时间
            if (specimenRequest.getCreationTime() != null) {
                specimen.setCreationTime(specimenRequest.getCreationTime());
            } else {
                specimen.setCreationTime(Instant.now().toEpochMilli());
            }
            
            // 设置创建者ID
            specimen.setCreatedBy(userId);
            
            // 设置所属单位ID
            specimen.setSpecimenCompany(specimenRequest.getSpecimenCompany());
            
            // 设置可选字段
            specimen.setCutShape(specimenRequest.getCutShape() != null ? 
                    specimenRequest.getCutShape() : "rectangle");
            specimen.setCutCount(specimenRequest.getCutCount() != null ? 
                    specimenRequest.getCutCount() : 1);
            specimen.setLength(specimenRequest.getLength() != null ? 
                    specimenRequest.getLength() : 0f);
            specimen.setWidth(specimenRequest.getWidth() != null ? 
                    specimenRequest.getWidth() : 0f);
            specimen.setHeight(specimenRequest.getHeight() != null ? 
                    specimenRequest.getHeight() : 0f);
            specimen.setRadius(specimenRequest.getRadius() != null ? 
                    specimenRequest.getRadius() : 0f);

            // 保存之前打印调试信息
            System.out.println("准备保存试件: " + specimen);
            
            // 在新的事务中保存试件
            Specimen savedSpecimen = saveSpecimenInTransaction(specimen);
            return convertToSpecimenResponse(savedSpecimen);
        } catch (Exception e) {
            // 捕获所有异常，打印详细信息，并重新抛出
            System.err.println("创建试件时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 在新的事务中保存试件
     * 使用REQUIRES_NEW确保每次都创建新事务，不受调用方事务影响
     * @param specimen 要保存的试件
     * @return 保存后的试件
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Specimen saveSpecimenInTransaction(Specimen specimen) {
        try {
            return specimenRepository.save(specimen);
        } catch (Exception e) {
            System.err.println("保存试件事务内部异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 批量创建试件
     * @param specimenRequests 试件请求列表
     * @return 创建的试件响应列表
     */
    public List<SpecimenResponse> createSpecimens(List<SpecimenRequest> specimenRequests) {
        return specimenRequests.stream()
                .map(this::createSpecimen)
                .collect(Collectors.toList());
    }

    /**
     * 更新试件
     * @param id 试件ID
     * @param specimenRequest 试件请求数据
     * @return 更新后的试件响应
     */
    public SpecimenResponse updateSpecimen(Long id, SpecimenRequest specimenRequest) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试件不存在，ID: " + id));

        // 更新基本信息
        specimen.setMixingTemperature(specimenRequest.getMixingTemperature());
        specimen.setMixingSpeed(specimenRequest.getMixingSpeed());
        specimen.setMixingTime(specimenRequest.getMixingTime());
        specimen.setCompactionMethod(specimenRequest.getCompactionMethod());
        
        // 更新可选字段
        if (specimenRequest.getCutShape() != null) {
            specimen.setCutShape(specimenRequest.getCutShape());
        }
        if (specimenRequest.getCutCount() != null) {
            specimen.setCutCount(specimenRequest.getCutCount());
        }
        if (specimenRequest.getLength() != null) {
            specimen.setLength(specimenRequest.getLength());
        }
        if (specimenRequest.getWidth() != null) {
            specimen.setWidth(specimenRequest.getWidth());
        }
        if (specimenRequest.getHeight() != null) {
            specimen.setHeight(specimenRequest.getHeight());
        }
        if (specimenRequest.getRadius() != null) {
            specimen.setRadius(specimenRequest.getRadius());
        }

        Specimen updatedSpecimen = specimenRepository.save(specimen);
        return convertToSpecimenResponse(updatedSpecimen);
    }

    /**
     * 删除试件
     * @param id 试件ID
     */
    public void deleteSpecimen(Long id) {
        if (!specimenRepository.existsById(id)) {
            throw new RuntimeException("试件不存在，ID: " + id);
        }
        specimenRepository.deleteById(id);
    }

    /**
     * 将Specimen实体转换为SpecimenResponse
     * @param specimen 试件实体
     * @return 试件响应
     */
    private SpecimenResponse convertToSpecimenResponse(Specimen specimen) {
        SpecimenResponse response = new SpecimenResponse();
        response.setId(specimen.getId());
        response.setMixRatioId(specimen.getMixRatioId());
        response.setMixingTemperature(specimen.getMixingTemperature());
        response.setMixingSpeed(specimen.getMixingSpeed());
        response.setMixingTime(specimen.getMixingTime());
        response.setCompactionMethod(specimen.getCompactionMethod());
        response.setCreationTime(specimen.getCreationTime());
        response.setCutShape(specimen.getCutShape());
        response.setCutCount(specimen.getCutCount());
        response.setLength(specimen.getLength());
        response.setWidth(specimen.getWidth());
        response.setHeight(specimen.getHeight());
        response.setRadius(specimen.getRadius());
        
        // 尝试获取配比名称
        try {
            MixRatio mixRatio = mixRatioRepository.findById(specimen.getMixRatioId()).orElse(null);
            if (mixRatio != null) {
                response.setMixRatioName(mixRatio.getMixName());
            }
        } catch (Exception e) {
            // 忽略配比名称获取失败的情况
        }
        
        return response;
    }

    /**
     * 获取当前登录用户的ID
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                // 如果未认证，返回默认用户ID或抛出异常
                System.out.println("警告: 无法获取当前用户认证信息，可能需要重新登录");
                return 1L; // 返回默认用户ID（应该是系统中确实存在的用户）
            }
            
            String userIdStr = authentication.getName();
            System.out.println("当前用户ID字符串: " + userIdStr);
            
            try {
                Long userId = Long.parseLong(userIdStr);
                
                // 验证用户ID是否存在
                if (!userRepository.existsById(userId)) {
                    System.out.println("警告: JWT中的用户ID (" + userId + ") 在数据库中不存在，使用默认用户ID");
                    return 1L; // 返回默认用户ID（应该是系统中确实存在的用户）
                }
                
                return userId;
            } catch (NumberFormatException e) {
                System.out.println("警告: 无法将用户ID字符串转换为数字: " + userIdStr);
                return 1L; // 返回默认用户ID
            }
        } catch (Exception e) {
            System.out.println("获取当前用户ID时发生异常: " + e.getMessage());
            e.printStackTrace();
            return 1L; // 返回默认用户ID
        }
    }
}
