package com.example.labdata.controller;

import com.example.labdata.model.Material;
import com.example.labdata.payload.ApiResponse;
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
@RequestMapping("/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createMaterial(@Valid @RequestBody Material material) {
        Material savedMaterial = materialService.createMaterial(material);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMaterial.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedMaterial);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @Valid @RequestBody Material materialDetails) {
        Material updatedMaterial = materialService.updateMaterial(id, materialDetails);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(new ApiResponse(true, "Material deleted successfully"));
    }

    @GetMapping("/type/{type}")
    public List<Material> getMaterialsByType(@PathVariable String type) {
        return materialService.findMaterialsByType(type);
    }

    // 同步相关端点
    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<Material> getMaterialsModifiedSince(@RequestParam("since") Instant since) {
        return materialService.findModifiedSince(since);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Material> syncMaterial(@Valid @RequestBody Material material) {
        Material syncedMaterial = materialService.syncMaterial(material);
        return ResponseEntity.ok(syncedMaterial);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncMaterials(@Valid @RequestBody List<Material> materials) {
        List<Material> syncedMaterials = materials.stream()
                .map(materialService::syncMaterial)
                .toList();
        return ResponseEntity.ok(syncedMaterials);
    }
}

