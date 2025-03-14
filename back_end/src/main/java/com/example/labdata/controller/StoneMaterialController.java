package com.example.labdata.controller;

import com.example.labdata.model.StoneMaterial;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.StoneMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/materials/stone")
public class StoneMaterialController {

    private final StoneMaterialService stoneMaterialService;

    @Autowired
    public StoneMaterialController(StoneMaterialService stoneMaterialService) {
        this.stoneMaterialService = stoneMaterialService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoneMaterial>>> getAllStoneMaterials(
            @RequestParam(required = false) String companyId) {
        List<StoneMaterial> materials;
        if (companyId != null && !companyId.isEmpty()) {
            materials = stoneMaterialService.getStoneMaterialsByCompany(companyId);
        } else {
            materials = stoneMaterialService.getAllStoneMaterials();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "石子材料获取成功", materials));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoneMaterial>> getStoneMaterialById(@PathVariable Long id) {
        try {
            StoneMaterial material = stoneMaterialService.getStoneMaterialById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "石子原料获取成功", material));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoneMaterial>> createStoneMaterial(
            @Valid @RequestBody StoneMaterial stoneMaterial) {
        try {
            StoneMaterial savedMaterial = stoneMaterialService.createStoneMaterial(stoneMaterial);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "石子原料创建成功", savedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "石子原料创建失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoneMaterial>> updateStoneMaterial(
            @PathVariable Long id,
            @Valid @RequestBody StoneMaterial stoneMaterial) {
        try {
            StoneMaterial updatedMaterial = stoneMaterialService.updateStoneMaterial(id, stoneMaterial);
            return ResponseEntity.ok(new ApiResponse<>(true, "石子原料更新成功", updatedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "石子原料更新失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStoneMaterial(@PathVariable Long id) {
        try {
            stoneMaterialService.deleteStoneMaterial(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "石子原料删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "石子原料删除失败: " + e.getMessage(), null));
        }
    }
}
