package com.example.labdata.controller;

import com.example.labdata.model.ExperimentType;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.service.ExperimentTypeService;
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
@RequestMapping("/api/experiment-types")
public class ExperimentTypeController {

    @Autowired
    private ExperimentTypeService experimentTypeService;

    @GetMapping
    public List<ExperimentType> getAllExperimentTypes() {
        return experimentTypeService.getAllExperimentTypes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperimentType> getExperimentTypeById(@PathVariable Long id) {
        ExperimentType experimentType = experimentTypeService.getExperimentTypeById(id);
        return ResponseEntity.ok(experimentType);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createExperimentType(@Valid @RequestBody ExperimentType experimentType) {
        ExperimentType savedExperimentType = experimentTypeService.createExperimentType(experimentType);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedExperimentType.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedExperimentType);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExperimentType> updateExperimentType(
            @PathVariable Long id, @Valid @RequestBody ExperimentType experimentTypeDetails) {
        ExperimentType updatedExperimentType = experimentTypeService.updateExperimentType(id, experimentTypeDetails);
        return ResponseEntity.ok(updatedExperimentType);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteExperimentType(@PathVariable Long id) {
        experimentTypeService.deleteExperimentType(id);
        return ResponseEntity.ok(new ApiResponse(true, "Experiment type deleted successfully"));
    }

    // 同步相关端点
    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<ExperimentType> getExperimentTypesModifiedSince(@RequestParam("since") Instant since) {
        return experimentTypeService.findModifiedSince(since);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExperimentType> syncExperimentType(@Valid @RequestBody ExperimentType experimentType) {
        ExperimentType syncedExperimentType = experimentTypeService.syncExperimentType(experimentType);
        return ResponseEntity.ok(syncedExperimentType);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncExperimentTypes(@Valid @RequestBody List<ExperimentType> experimentTypes) {
        List<ExperimentType> syncedExperimentTypes = experimentTypes.stream()
                .map(experimentTypeService::syncExperimentType)
                .toList();
        return ResponseEntity.ok(syncedExperimentTypes);
    }
}

