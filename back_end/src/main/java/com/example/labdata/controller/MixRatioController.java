package com.example.labdata.controller;

import com.example.labdata.payload.request.MixRatioRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.MixRatioResponse;
import com.example.labdata.service.MixRatioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mixratios")
public class MixRatioController {

    private final MixRatioService mixRatioService;

    @Autowired
    public MixRatioController(MixRatioService mixRatioService) {
        this.mixRatioService = mixRatioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MixRatioResponse>>> getAllMixRatios() {
        List<MixRatioResponse> mixRatios = mixRatioService.getAllMixRatios();
        return ResponseEntity.ok(new ApiResponse<>(true, "配合比获取成功", mixRatios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MixRatioResponse>> getMixRatioById(@PathVariable Long id) {
        try {
            MixRatioResponse mixRatio = mixRatioService.getMixRatioById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "配合比获取成功", mixRatio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/code/{mixId}")
    public ResponseEntity<ApiResponse<MixRatioResponse>> getMixRatioByMixId(@PathVariable String mixId) {
        try {
            MixRatioResponse mixRatio = mixRatioService.getMixRatioByMixId(mixId);
            return ResponseEntity.ok(new ApiResponse<>(true, "配合比获取成功", mixRatio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MixRatioResponse>> createMixRatio(@Valid @RequestBody MixRatioRequest mixRatioRequest) {
        try {
            MixRatioResponse savedMixRatio = mixRatioService.createMixRatio(mixRatioRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "配合比创建成功", savedMixRatio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "配合比创建失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MixRatioResponse>> updateMixRatio(
            @PathVariable Long id,
            @Valid @RequestBody MixRatioRequest mixRatioRequest) {
        try {
            MixRatioResponse updatedMixRatio = mixRatioService.updateMixRatio(id, mixRatioRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "配合比更新成功", updatedMixRatio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "配合比更新失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMixRatio(@PathVariable Long id) {
        try {
            mixRatioService.deleteMixRatio(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "配合比删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "配合比删除失败: " + e.getMessage(), null));
        }
    }
}
