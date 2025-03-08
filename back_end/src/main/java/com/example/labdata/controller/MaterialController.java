package com.example.labdata.controller;

import com.example.labdata.model.Material;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<Material> getMaterialsByOrganization(@CurrentUser UserPrincipal currentUser) {
        // 获取用户所在组织的材料列表
        return materialService.getMaterialsByOrganization(currentUser.getOrganizationId());
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Material> getAllMaterials() {
        // 管理员可以查看所有材料
        return materialService.getAllMaterials();
    }
    
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER')")
    public List<Material> getMaterialsByType(@PathVariable String type, @CurrentUser UserPrincipal currentUser) {
        // 按类型获取用户所在组织的材料
        return materialService.getMaterialsByTypeAndOrganization(type, currentUser.getOrganizationId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Material material = materialService.getMaterialById(id);
        
        // 检查是否属于当前用户所在组织
        if (material.getOrganizationId() != null && !material.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(material);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createMaterial(@Valid @RequestBody Material material, @CurrentUser UserPrincipal currentUser) {
        // 设置组织ID为当前用户所在组织
        material.setOrganizationId(currentUser.getOrganizationId());
        Material savedMaterial = materialService.createMaterial(material);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMaterial.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedMaterial);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @Valid @RequestBody Material materialDetails, @CurrentUser UserPrincipal currentUser) {
        Material existingMaterial = materialService.getMaterialById(id);
        
        // 检查是否属于当前用户所在组织
        if (existingMaterial.getOrganizationId() != null && !existingMaterial.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        // 确保不修改组织ID
        materialDetails.setOrganizationId(currentUser.getOrganizationId());
        
        Material updatedMaterial = materialService.updateMaterial(id, materialDetails);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Material material = materialService.getMaterialById(id);
        
        // 检查是否属于当前用户所在组织
        if (material.getOrganizationId() != null && !material.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        materialService.deleteMaterial(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<Material> getMaterialsModifiedSince(@RequestParam("since") Instant since, @CurrentUser UserPrincipal currentUser) {
        return materialService.findModifiedSince(since, currentUser.getOrganizationId());
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Material> syncMaterial(@Valid @RequestBody Material material, @CurrentUser UserPrincipal currentUser) {
        material.setOrganizationId(currentUser.getOrganizationId());
        Material syncedMaterial = materialService.syncMaterial(material);
        return ResponseEntity.ok(syncedMaterial);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncMaterials(@Valid @RequestBody List<Material> materials, @CurrentUser UserPrincipal currentUser) {
        materials.forEach(material -> material.setOrganizationId(currentUser.getOrganizationId()));
        List<Material> syncedMaterials = materials.stream()
                .map(materialService::syncMaterial)
                .toList();
        return ResponseEntity.ok(syncedMaterials);
    }
}
