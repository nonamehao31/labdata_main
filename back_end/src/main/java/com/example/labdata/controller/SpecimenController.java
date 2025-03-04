package com.example.labdata.controller;

import com.example.labdata.model.Specimen;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.service.SpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/specimens")
public class SpecimenController {

    @Autowired
    private SpecimenService specimenService;

    @GetMapping
    public List<Specimen> getAllSpecimens() {
        return specimenService.getAllSpecimens();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specimen> getSpecimenById(@PathVariable Long id) {
        Specimen specimen = specimenService.getSpecimenById(id);
        return ResponseEntity.ok(specimen);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Specimen> getSpecimenByCode(@PathVariable String code) {
        Optional<Specimen> specimen = specimenService.findByCode(code);
        return specimen.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSpecimen(@Valid @RequestBody Specimen specimen) {
        Specimen savedSpecimen = specimenService.createSpecimen(specimen);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedSpecimen.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedSpecimen);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Specimen> updateSpecimen(@PathVariable Long id, @Valid @RequestBody Specimen specimenDetails) {
        Specimen updatedSpecimen = specimenService.updateSpecimen(id, specimenDetails);
        return ResponseEntity.ok(updatedSpecimen);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSpecimen(@PathVariable Long id) {
        specimenService.deleteSpecimen(id);
        return ResponseEntity.ok(new ApiResponse(true, "Specimen deleted successfully"));
    }

    @GetMapping("/mix-ratio/{mixRatioId}")
    public List<Specimen> getSpecimensByMixRatioId(@PathVariable Long mixRatioId) {
        return specimenService.findSpecimensByMixRatioId(mixRatioId);
    }

    // 同步相关端点
    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<Specimen> getSpecimensModifiedSince(@RequestParam("since") Instant since) {
        return specimenService.findModifiedSince(since);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Specimen> syncSpecimen(@Valid @RequestBody Specimen specimen) {
        Specimen syncedSpecimen = specimenService.syncSpecimen(specimen);
        return ResponseEntity.ok(syncedSpecimen);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncSpecimens(@Valid @RequestBody List<Specimen> specimens) {
        List<Specimen> syncedSpecimens = specimens.stream()
                .map(specimenService::syncSpecimen)
                .toList();
        return ResponseEntity.ok(syncedSpecimens);
    }
}

