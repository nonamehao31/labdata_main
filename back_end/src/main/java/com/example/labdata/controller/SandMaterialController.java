package com.example.labdata.controller;

import com.example.labdata.model.SandMaterial;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.SandMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/materials/sand")
public class SandMaterialController {

    private final SandMaterialService sandMaterialService;

    @Autowired
    public SandMaterialController(SandMaterialService sandMaterialService) {
        this.sandMaterialService = sandMaterialService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SandMaterial>>> getAllSandMaterials(
            @RequestParam(required = false) String companyId) {
        List<SandMaterial> materials;
        if (companyId != null && !companyId.isEmpty()) {
            materials = sandMaterialService.getSandMaterialsByCompany(companyId);
        } else {
            materials = sandMaterialService.getAllSandMaterials();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "沙子材料获取成功", materials));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SandMaterial>> getSandMaterialById(@PathVariable Long id) {
        try {
            SandMaterial material = sandMaterialService.getSandMaterialById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "沙子原料获取成功", material));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SandMaterial>> createSandMaterial(
            @Valid @RequestBody SandMaterial sandMaterial) {
        try {
            SandMaterial savedMaterial = sandMaterialService.createSandMaterial(sandMaterial);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "沙子原料创建成功", savedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沙子原料创建失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SandMaterial>> updateSandMaterial(
            @PathVariable Long id,
            @Valid @RequestBody SandMaterial sandMaterial) {
        try {
            SandMaterial updatedMaterial = sandMaterialService.updateSandMaterial(id, sandMaterial);
            return ResponseEntity.ok(new ApiResponse<>(true, "沙子原料更新成功", updatedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沙子原料更新失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSandMaterial(@PathVariable Long id) {
        try {
            sandMaterialService.deleteSandMaterial(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "沙子原料删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沙子原料删除失败: " + e.getMessage(), null));
        }
    }
}
