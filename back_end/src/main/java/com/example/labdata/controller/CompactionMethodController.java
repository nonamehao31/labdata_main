package com.example.labdata.controller;

import com.example.labdata.model.CompactionMethod;
import com.example.labdata.model.Specimen;
import com.example.labdata.model.User;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.CompactionMethodResponse;
import com.example.labdata.repository.CompactionMethodRepository;
import com.example.labdata.repository.SpecimenRepository;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 制件方法控制器
 * 提供用户所属单位的制件方法相关API
 */
@RestController
@RequestMapping("/api/compaction-methods")
public class CompactionMethodController {

    private static final Logger logger = LoggerFactory.getLogger(CompactionMethodController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecimenRepository specimenRepository;

    @Autowired
    private CompactionMethodRepository compactionMethodRepository;

    /**
     * 获取当前用户所属单位的制件方法列表
     * @param currentUser 当前登录用户
     * @return 制件方法列表
     */
    @GetMapping("/organization")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<CompactionMethodResponse>>> getOrganizationCompactionMethods(
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("获取用户(ID:{})所属单位的制件方法列表", currentUser.getId());
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", Collections.emptyList()));
            }
            
            User user = userOpt.get();
            String organization = user.getOrganization();
            Long organizationId = user.getOrganizationId();
            
            if (organization == null || organization.isEmpty() || organizationId == null) {
                logger.warn("用户(ID:{})没有设置单位信息", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户单位未设置", Collections.emptyList()));
            }
            
            // 直接从specimens表中获取不同的压实方法
            List<String> distinctMethods = specimenRepository.findDistinctCompactionMethodsBySpecimenCompany(organizationId);
            logger.info("从specimens表中找到{}个不同的制件方法，单位ID: {}", distinctMethods.size(), organizationId);
            
            // 转换为响应DTO
            List<CompactionMethodResponse> responseList = distinctMethods.stream()
                    .filter(method -> method != null && !method.isEmpty())
                    .map(methodName -> {
                        // 创建一个响应对象，包含拌合参数
                        CompactionMethodResponse response = new CompactionMethodResponse();
                        response.setCompactionMethod(methodName);
                        response.setMethodName(methodName);
                        response.setOrganization(organization);
                        
                        // 获取最新的拌合参数
                        Specimen latestSpecimen = specimenRepository.findLatestSpecimenByCompanyAndMethod(organizationId, methodName);
                        if (latestSpecimen != null) {
                            logger.info("找到制件方法[{}]的拌合参数，温度: {}, 速度: {}, 时间: {}", 
                                      methodName, 
                                      latestSpecimen.getMixingTemperature(), 
                                      latestSpecimen.getMixingSpeed(), 
                                      latestSpecimen.getMixingTime());
                            
                            // 设置拌合参数
                            response.setMixingTemperature(latestSpecimen.getMixingTemperature());
                            response.setMixingSpeed(latestSpecimen.getMixingSpeed());
                            response.setMixingTime(latestSpecimen.getMixingTime() != null ? latestSpecimen.getMixingTime().floatValue() : null);
                            
                            // 设置唯一ID - 使用specimen的ID为制件方法提供唯一标识
                            response.setId(latestSpecimen.getId());
                            
                            logger.info("为制件方法[{}]设置ID: {}", methodName, latestSpecimen.getId());
                        } else {
                            // 如果找不到对应的specimen记录，生成一个新的ID
                            // 这里使用方法名的哈希码作为ID，以确保同名方法总是有相同的ID
                            long hashId = Math.abs(methodName.hashCode());
                            response.setId(hashId);
                            logger.info("无法找到制件方法[{}]对应的specimen记录，使用哈希ID: {}", methodName, hashId);
                        }
                        
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "成功从specimens表获取制件方法列表", responseList));
            
        } catch (Exception e) {
            logger.error("获取制件方法列表时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取制件方法列表失败: " + e.getMessage(), Collections.emptyList()));
        }
    }
    
    /**
     * 获取制件方法列表
     * @param organizationId 单位ID
     * @return 制件方法列表
     */
    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<CompactionMethodResponse>>> getCompactionMethods(
            @RequestParam(name = "organizationId", required = false) Long organizationId) {
        
        logger.info("根据单位ID({})获取制件方法列表", organizationId);
        
        try {
            if (organizationId == null) {
                logger.error("单位ID不能为空");
                return ResponseEntity.ok(new ApiResponse<>(false, "单位ID不能为空", Collections.emptyList()));
            }
            
            // 直接从specimens表中获取不同的压实方法
            List<String> distinctMethods = specimenRepository.findDistinctCompactionMethodsBySpecimenCompany(organizationId);
            logger.info("从specimens表中找到{}个不同的制件方法，单位ID: {}", distinctMethods.size(), organizationId);
            
            // 转换为响应DTO
            List<CompactionMethodResponse> responseList = distinctMethods.stream()
                    .filter(method -> method != null && !method.isEmpty())
                    .map(methodName -> {
                        // 创建一个响应对象，包含拌合参数
                        CompactionMethodResponse response = new CompactionMethodResponse();
                        response.setCompactionMethod(methodName);
                        response.setMethodName(methodName);
                        
                        // 获取最新的拌合参数
                        Specimen latestSpecimen = specimenRepository.findLatestSpecimenByCompanyAndMethod(organizationId, methodName);
                        if (latestSpecimen != null) {
                            logger.info("找到制件方法[{}]的拌合参数，温度: {}, 速度: {}, 时间: {}", 
                                      methodName, 
                                      latestSpecimen.getMixingTemperature(), 
                                      latestSpecimen.getMixingSpeed(), 
                                      latestSpecimen.getMixingTime());
                            
                            // 设置拌合参数
                            response.setMixingTemperature(latestSpecimen.getMixingTemperature());
                            response.setMixingSpeed(latestSpecimen.getMixingSpeed());
                            response.setMixingTime(latestSpecimen.getMixingTime() != null ? latestSpecimen.getMixingTime().floatValue() : null);
                        } else {
                            logger.warn("未找到制件方法[{}]的拌合参数记录", methodName);
                        }
                        
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "成功从specimens表获取制件方法列表", responseList));
            
        } catch (Exception e) {
            logger.error("获取制件方法列表时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取制件方法列表失败: " + e.getMessage(), Collections.emptyList()));
        }
    }
}
