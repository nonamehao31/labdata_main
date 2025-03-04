package com.example.labdata.controller;

import com.example.labdata.payload.SyncRequest;
import com.example.labdata.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    @Autowired
    private SyncService syncService;

    /**
     * 获取自上次同步以来的所有数据变�?     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAllChangesSince(@RequestParam("since") Instant lastSyncTime) {
        Map<String, Object> changes = syncService.getAllChangesSince(lastSyncTime);
        return ResponseEntity.ok(changes);
    }

    /**
     * 处理客户端提交的同步数据
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> processSyncData(@Valid @RequestBody SyncRequest syncRequest) {
        Map<String, Object> result = syncService.processSyncData(syncRequest.getData());
        return ResponseEntity.ok(result);
    }
}

