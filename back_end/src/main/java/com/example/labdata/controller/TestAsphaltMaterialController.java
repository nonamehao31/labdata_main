package com.example.labdata.controller;

import com.example.labdata.model.TestAsphaltMaterial;
import com.example.labdata.model.User;
import com.example.labdata.payload.request.TestAsphaltMaterialRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.TestAsphaltMaterialResponse;
import com.example.labdata.repository.TestAsphaltMaterialRepository;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 沥青材料控制器
 */
@RestController
@RequestMapping("/api/test-asphalt-materials")
public class TestAsphaltMaterialController {

    private static final Logger logger = LoggerFactory.getLogger(TestAsphaltMaterialController.class);

    @Autowired
    private TestAsphaltMaterialRepository testAsphaltMaterialRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取用户所属单位的所有沥青材料
     * @param currentUser 当前用户
     * @return 沥青材料列表
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TestAsphaltMaterialResponse>>> getAllAsphaltMaterials(
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("获取用户(ID:{})所属单位的沥青材料列表", currentUser.getId());
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", Collections.emptyList()));
            }
            
            User user = userOpt.get();
            Long organizationId = user.getOrganizationId();
            
            if (organizationId == null) {
                logger.warn("用户(ID:{})没有设置单位信息", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户单位未设置", Collections.emptyList()));
            }
            
            // 查询该单位的所有沥青材料
            List<TestAsphaltMaterial> materials = testAsphaltMaterialRepository.findByOrganizationId(organizationId);
            List<TestAsphaltMaterialResponse> responseList = TestAsphaltMaterialResponse.fromEntities(materials);
            
            logger.info("成功获取到{}条沥青材料记录", responseList.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", responseList));
            
        } catch (Exception e) {
            logger.error("获取沥青材料列表时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取沥青材料失败: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * 获取单位中未过期的沥青材料
     * @param currentUser 当前用户
     * @return 未过期的沥青材料列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TestAsphaltMaterialResponse>>> getActiveAsphaltMaterials(
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("获取用户(ID:{})所属单位的未过期沥青材料列表", currentUser.getId());
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", Collections.emptyList()));
            }
            
            User user = userOpt.get();
            Long organizationId = user.getOrganizationId();
            
            if (organizationId == null) {
                logger.warn("用户(ID:{})没有设置单位信息", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户单位未设置", Collections.emptyList()));
            }
            
            // 查询未过期的沥青材料
            LocalDate currentDate = LocalDate.now();
            List<TestAsphaltMaterial> materials = testAsphaltMaterialRepository.findActiveByOrganizationId(organizationId, currentDate);
            List<TestAsphaltMaterialResponse> responseList = TestAsphaltMaterialResponse.fromEntities(materials);
            
            logger.info("成功获取到{}条未过期沥青材料记录", responseList.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", responseList));
            
        } catch (Exception e) {
            logger.error("获取未过期沥青材料列表时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取未过期沥青材料失败: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * 保存沥青材料
     * @param currentUser 当前用户
     * @param request 沥青材料请求
     * @return 保存结果
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TestAsphaltMaterialResponse>> saveAsphaltMaterial(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TestAsphaltMaterialRequest request) {
        
        logger.info("接收到沥青材料保存请求：供应商={}, 标号={}, 类型={}", 
                request.getAsphaltSupplier(), request.getAsphaltGrade(), request.getAsphaltCatalog());
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", null));
            }
            
            User user = userOpt.get();
            Long organizationId = user.getOrganizationId();
            
            if (organizationId == null) {
                logger.warn("用户(ID:{})没有设置单位信息", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户单位未设置", null));
            }
            
            // 创建新的沥青材料实体
            TestAsphaltMaterial material = new TestAsphaltMaterial();
            material.setAsphaltSupplier(request.getAsphaltSupplier());
            material.setAsphaltTestDue(request.getAsphaltTestDue());
            material.setAsphaltGrade(request.getAsphaltGrade());
            material.setAsphaltCatalog(request.getAsphaltCatalog());
            material.setOrganizationId(organizationId);
            material.setCreatedBy(currentUser.getId());
            
            // 保存到数据库
            TestAsphaltMaterial savedMaterial = testAsphaltMaterialRepository.save(material);
            TestAsphaltMaterialResponse response = TestAsphaltMaterialResponse.fromEntity(savedMaterial);
            
            logger.info("成功保存沥青材料，ID={}", savedMaterial.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "保存成功", response));
            
        } catch (Exception e) {
            logger.error("保存沥青材料时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "保存沥青材料失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据ID获取沥青材料
     * @param currentUser 当前用户
     * @param id 沥青材料ID
     * @return 沥青材料
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TestAsphaltMaterialResponse>> getAsphaltMaterialById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        
        logger.info("获取沥青材料，ID={}", id);
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", null));
            }
            
            User user = userOpt.get();
            Long organizationId = user.getOrganizationId();
            
            // 查询沥青材料
            Optional<TestAsphaltMaterial> materialOpt = testAsphaltMaterialRepository.findById(id);
            
            if (!materialOpt.isPresent()) {
                logger.warn("未找到沥青材料，ID={}", id);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到沥青材料", null));
            }
            
            TestAsphaltMaterial material = materialOpt.get();
            
            // 验证是否属于同一单位
            if (!material.getOrganizationId().equals(organizationId)) {
                logger.warn("用户无权访问其他单位的沥青材料，材料ID={}，材料单位={}，用户单位={}", 
                        id, material.getOrganizationId(), organizationId);
                return ResponseEntity.ok(new ApiResponse<>(false, "无权访问", null));
            }
            
            TestAsphaltMaterialResponse response = TestAsphaltMaterialResponse.fromEntity(material);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", response));
            
        } catch (Exception e) {
            logger.error("获取沥青材料时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取沥青材料失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据类型获取沥青材料
     * @param currentUser 当前用户
     * @param catalog 沥青类型
     * @return 沥青材料列表
     */
    @GetMapping("/catalog/{catalog}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TestAsphaltMaterialResponse>>> getAsphaltMaterialsByType(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String catalog) {
        
        logger.info("获取类型为{}的沥青材料列表", catalog);
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            
            if (!userOpt.isPresent()) {
                logger.error("用户不存在: {}", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", Collections.emptyList()));
            }
            
            User user = userOpt.get();
            Long organizationId = user.getOrganizationId();
            
            if (organizationId == null) {
                logger.warn("用户(ID:{})没有设置单位信息", currentUser.getId());
                return ResponseEntity.ok(new ApiResponse<>(false, "用户单位未设置", Collections.emptyList()));
            }
            
            // 查询指定类型的沥青材料
            List<TestAsphaltMaterial> materials = testAsphaltMaterialRepository
                    .findByOrganizationIdAndAsphaltCatalog(organizationId, catalog);
            List<TestAsphaltMaterialResponse> responseList = TestAsphaltMaterialResponse.fromEntities(materials);
            
            logger.info("成功获取到{}条类型为{}的沥青材料记录", responseList.size(), catalog);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", responseList));
            
        } catch (Exception e) {
            logger.error("获取沥青材料列表时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取沥青材料失败: " + e.getMessage(), Collections.emptyList()));
        }
    }
}
