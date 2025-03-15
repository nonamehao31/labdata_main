package com.example.labdata.controller;

import com.example.labdata.payload.request.SpecimenRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.SpecimenResponse;
import com.example.labdata.service.SpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/specimens")
public class SpecimenController {

    private final SpecimenService specimenService;

    @Autowired
    public SpecimenController(SpecimenService specimenService) {
        this.specimenService = specimenService;
    }

    /**
     * 根据配比ID获取所有试件
     * @param mixRatioId 配比ID
     * @return 试件列表响应
     */
    @GetMapping("/mixratio/{mixRatioId}")
    public ResponseEntity<ApiResponse<List<SpecimenResponse>>> getSpecimensByMixRatioId(@PathVariable Long mixRatioId) {
        try {
            List<SpecimenResponse> specimens = specimenService.getSpecimensByMixRatioId(mixRatioId);
            return ResponseEntity.ok(new ApiResponse<>(true, "试件获取成功", specimens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "试件获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据ID获取试件
     * @param id 试件ID
     * @return 试件响应
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecimenResponse>> getSpecimenById(@PathVariable Long id) {
        try {
            SpecimenResponse specimen = specimenService.getSpecimenById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "试件获取成功", specimen));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * 创建新试件
     * @param specimenRequest 试件请求数据
     * @return 创建的试件响应
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SpecimenResponse>> createSpecimen(@Valid @RequestBody SpecimenRequest specimenRequest) {
        try {
            // 打印收到的请求数据，便于调试
            System.out.println("收到创建试件请求: " + specimenRequest);
            
            // 调用Service层处理业务逻辑和事务
            SpecimenResponse savedSpecimen = specimenService.createSpecimen(specimenRequest);
            
            // 返回成功响应
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "试件创建成功", savedSpecimen));
        } catch (Exception e) {
            // 增强错误日志，打印完整的异常栈和请求数据
            System.err.println("创建试件失败，详细信息如下:");
            System.err.println("请求数据: " + specimenRequest);
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "试件创建失败: " + e.getMessage(), null));
        }
    }

    /**
     * 批量创建试件
     * @param specimenRequests 试件请求列表
     * @return 创建的试件响应列表
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<SpecimenResponse>>> createSpecimens(@Valid @RequestBody List<SpecimenRequest> specimenRequests) {
        try {
            List<SpecimenResponse> savedSpecimens = specimenService.createSpecimens(specimenRequests);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "试件批量创建成功", savedSpecimens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "试件批量创建失败: " + e.getMessage(), null));
        }
    }

    /**
     * 更新试件
     * @param id 试件ID
     * @param specimenRequest 试件请求数据
     * @return 更新后的试件响应
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecimenResponse>> updateSpecimen(
            @PathVariable Long id,
            @Valid @RequestBody SpecimenRequest specimenRequest) {
        try {
            SpecimenResponse updatedSpecimen = specimenService.updateSpecimen(id, specimenRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "试件更新成功", updatedSpecimen));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "试件更新失败: " + e.getMessage(), null));
        }
    }

    /**
     * 删除试件
     * @param id 试件ID
     * @return 删除响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpecimen(@PathVariable Long id) {
        try {
            specimenService.deleteSpecimen(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "试件删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "试件删除失败: " + e.getMessage(), null));
        }
    }
}
