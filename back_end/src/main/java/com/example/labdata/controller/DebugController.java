package com.example.labdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * u7528u4e8eu8c03u8bd5u7684APIu63a7u5236u5668
 * u63d0u4f9bu670du52a1u5668u65f6u95f4u4fe1u606fu7b49u8c03u8bd5u5de5u5177
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    /**
     * u83b7u53d6u670du52a1u5668u5f53u524du65f6u95f4u4fe1u606f
     * @return u670du52a1u5668u7684u65f6u95f4u4fe1u606f
     */
    @GetMapping("/server-time")
    public ResponseEntity<?> getServerTime() {
        // u83b7u53d6u5f53u524du65f6u95f4
        Instant now = Instant.now();
        long epochMillis = now.toEpochMilli();
        long epochSeconds = now.getEpochSecond();
        
        // u83b7u53d6u4e0du540cu65f6u533au7684u65f6u95f4
        ZonedDateTime utcTime = now.atZone(ZoneId.of("UTC"));
        ZonedDateTime shanghaiTime = now.atZone(ZoneId.of("Asia/Shanghai"));
        
        // u683cu5f0fu5316u65f6u95f4
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String utcFormatted = utcTime.format(formatter);
        String shanghaiFormatted = shanghaiTime.format(formatter);
        
        // u6784u5efau54cdu5e94
        Map<String, Object> timeInfo = new HashMap<>();
        timeInfo.put("currentTimeMillis", epochMillis);
        timeInfo.put("currentTimeSeconds", epochSeconds);
        timeInfo.put("utcTime", utcFormatted);
        timeInfo.put("shanghaiTime", shanghaiFormatted);
        timeInfo.put("defaultTimeZone", ZoneId.systemDefault().getId());
        
        return ResponseEntity.ok(timeInfo);
    }
}
