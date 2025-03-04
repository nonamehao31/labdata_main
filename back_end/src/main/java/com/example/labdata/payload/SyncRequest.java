package com.example.labdata.payload;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
public class SyncRequest {
    
    @NotNull
    private Instant lastSyncTime;
    
    @NotNull
    private Map<String, Object> data;
}

