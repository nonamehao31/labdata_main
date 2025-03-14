package com.example.labdata.controller;

import com.example.labdata.model.AsphaltMaterial;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.AsphaltMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/materials/asphalt")
public class AsphaltMaterialController {

    private final AsphaltMaterialService asphaltMaterialService;

    @Autowired
    public AsphaltMaterialController(AsphaltMaterialService asphaltMaterialService) {
        this.asphaltMaterialService = asphaltMaterialService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AsphaltMaterial>>> getAllAsphaltMaterials(
            @RequestParam(required = false) String companyId) {
        List<AsphaltMaterial> materials;
        if (companyId != null && !companyId.isEmpty()) {
            materials = asphaltMaterialService.getAsphaltMaterialsByCompany(companyId);
        } else {
            materials = asphaltMaterialService.getAllAsphaltMaterials();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "沥青原料获取成功", materials));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AsphaltMaterial>> getAsphaltMaterialById(@PathVariable Long id) {
        try {
            AsphaltMaterial material = asphaltMaterialService.getAsphaltMaterialById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "沥青原料获取成功", material));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AsphaltMaterial>> createAsphaltMaterial(
            @Valid @RequestBody AsphaltMaterial asphaltMaterial) {
        try {
            AsphaltMaterial savedMaterial = asphaltMaterialService.createAsphaltMaterial(asphaltMaterial);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "沥青原料创建成功", savedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沥青原料创建失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AsphaltMaterial>> updateAsphaltMaterial(
            @PathVariable Long id,
            @Valid @RequestBody AsphaltMaterial asphaltMaterial) {
        try {
            AsphaltMaterial updatedMaterial = asphaltMaterialService.updateAsphaltMaterial(id, asphaltMaterial);
            return ResponseEntity.ok(new ApiResponse<>(true, "沥青原料更新成功", updatedMaterial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沥青原料更新失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAsphaltMaterial(@PathVariable Long id) {
        try {
            asphaltMaterialService.deleteAsphaltMaterial(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "沥青原料删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "沥青原料删除失败: " + e.getMessage(), null));
        }
    }
}
