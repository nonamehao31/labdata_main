package com.example.labdata.controller;

import com.example.labdata.model.MixRatio;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.MixRatioService;
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
@RequestMapping("/api/mix-ratios")
public class MixRatioController {

    @Autowired
    private MixRatioService mixRatioService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<MixRatio> getMixRatiosByOrganization(@CurrentUser UserPrincipal currentUser) {
        // 获取用户所在组织的配比列表
        return mixRatioService.getMixRatiosByOrganization(currentUser.getOrganizationId());
    }
    
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public List<MixRatio> getMixRatiosByProject(@PathVariable Long projectId, @CurrentUser UserPrincipal currentUser) {
        // 获取特定项目的配比列表，仅限于用户所在组织
        return mixRatioService.getMixRatiosByProjectAndOrganization(projectId, currentUser.getOrganizationId());
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MixRatio> getAllMixRatios() {
        // 管理员可以查看所有配比
        return mixRatioService.getAllMixRatios();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MixRatio> getMixRatioById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        MixRatio mixRatio = mixRatioService.getMixRatioById(id);
        
        // 检查是否属于当前用户所在组织
        if (mixRatio.getOrganizationId() != null && !mixRatio.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(mixRatio);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createMixRatio(@Valid @RequestBody MixRatio mixRatio, @CurrentUser UserPrincipal currentUser) {
        // 设置组织ID为当前用户所在组织
        mixRatio.setOrganizationId(currentUser.getOrganizationId());
        MixRatio savedMixRatio = mixRatioService.createMixRatio(mixRatio);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMixRatio.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedMixRatio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MixRatio> updateMixRatio(@PathVariable Long id, @Valid @RequestBody MixRatio mixRatioDetails, @CurrentUser UserPrincipal currentUser) {
        MixRatio existingMixRatio = mixRatioService.getMixRatioById(id);
        
        // 检查是否属于当前用户所在组织
        if (existingMixRatio.getOrganizationId() != null && !existingMixRatio.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        // 确保不修改组织ID
        mixRatioDetails.setOrganizationId(currentUser.getOrganizationId());
        
        MixRatio updatedMixRatio = mixRatioService.updateMixRatio(id, mixRatioDetails);
        return ResponseEntity.ok(updatedMixRatio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteMixRatio(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        MixRatio mixRatio = mixRatioService.getMixRatioById(id);
        
        // 检查是否属于当前用户所在组织
        if (mixRatio.getOrganizationId() != null && !mixRatio.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        mixRatioService.deleteMixRatio(id);
        return ResponseEntity.ok().build();
    }
    
    // 同步相关端点
    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<MixRatio> getMixRatiosModifiedSince(@RequestParam("since") Instant since, @CurrentUser UserPrincipal currentUser) {
        return mixRatioService.findModifiedSince(since, currentUser.getOrganizationId());
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MixRatio> syncMixRatio(@Valid @RequestBody MixRatio mixRatio, @CurrentUser UserPrincipal currentUser) {
        mixRatio.setOrganizationId(currentUser.getOrganizationId());
        MixRatio syncedMixRatio = mixRatioService.syncMixRatio(mixRatio);
        return ResponseEntity.ok(syncedMixRatio);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncMixRatios(@Valid @RequestBody List<MixRatio> mixRatios, @CurrentUser UserPrincipal currentUser) {
        mixRatios.forEach(mixRatio -> mixRatio.setOrganizationId(currentUser.getOrganizationId()));
        List<MixRatio> syncedMixRatios = mixRatios.stream()
                .map(mixRatioService::syncMixRatio)
                .toList();
        return ResponseEntity.ok(syncedMixRatios);
    }
}
