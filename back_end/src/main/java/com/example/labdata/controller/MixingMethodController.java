package com.example.labdata.controller;

import com.example.labdata.model.MixingMethod;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.MixingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mixing-methods")
public class MixingMethodController {

    private final MixingMethodService mixingMethodService;

    @Autowired
    public MixingMethodController(MixingMethodService mixingMethodService) {
        this.mixingMethodService = mixingMethodService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<MixingMethod> getMixingMethodsByOrganization(@CurrentUser UserPrincipal currentUser) {
        // 获取当前用户所在组织的制件方法列表
        return mixingMethodService.getAllMixingMethodsByOrganization(currentUser.getOrganizationId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MixingMethod> getMixingMethodById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        MixingMethod mixingMethod = mixingMethodService.getMixingMethodById(id);
        
        // 检查是否属于当前用户所在组织
        if (!mixingMethod.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(mixingMethod);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public MixingMethod createMixingMethod(@Valid @RequestBody MixingMethod mixingMethod, @CurrentUser UserPrincipal currentUser) {
        // 设置组织ID为当前用户所在组织
        mixingMethod.setOrganizationId(currentUser.getOrganizationId());
        return mixingMethodService.createMixingMethod(mixingMethod);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MixingMethod> updateMixingMethod(@PathVariable Long id, @Valid @RequestBody MixingMethod mixingMethodDetails, @CurrentUser UserPrincipal currentUser) {
        MixingMethod existingMethod = mixingMethodService.getMixingMethodById(id);
        
        // 检查是否属于当前用户所在组织
        if (!existingMethod.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        // 确保不修改组织ID
        mixingMethodDetails.setOrganizationId(currentUser.getOrganizationId());
        
        MixingMethod updatedMethod = mixingMethodService.updateMixingMethod(id, mixingMethodDetails);
        return ResponseEntity.ok(updatedMethod);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteMixingMethod(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        MixingMethod mixingMethod = mixingMethodService.getMixingMethodById(id);
        
        // 检查是否属于当前用户所在组织
        if (!mixingMethod.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        mixingMethodService.deleteMixingMethod(id);
        return ResponseEntity.ok().build();
    }
}
