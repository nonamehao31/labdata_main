package com.example.labdata.payload.response;

import com.example.labdata.model.MixRatio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MixRatioResponse {
    private Long id;
    private String mixName;
    private String mixId;
    private LocalDateTime createdAt;
    private List<AsphaltComponentResponse> asphaltComponents;
    private List<SandComponentResponse> sandComponents;
    private List<StoneComponentResponse> stoneComponents;
    
    public MixRatioResponse(MixRatio mixRatio) {
        this.id = mixRatio.getId();
        this.mixName = mixRatio.getMixName();
        this.mixId = mixRatio.getMixId();
        this.createdAt = mixRatio.getCreatedAt();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsphaltComponentResponse {
        private Long id;
        private Long asphaltId;
        private String asphaltName;
        private String asphaltGrade;
        private String asphaltCharacter;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SandComponentResponse {
        private Long id;
        private Long sandId;
        private String sandName;
        private String gradation;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoneComponentResponse {
        private Long id;
        private Long stoneId;
        private String stoneName;
        private String gradation;
        private Double percentage;
    }
}
