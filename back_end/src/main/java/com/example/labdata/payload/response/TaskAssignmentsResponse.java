package com.example.labdata.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;

/**
 * 任务实验指派响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentsResponse {
    
    /**
     * 配比ID与实验指派列表的映射
     * 键为mixratioId，值为该配比对应的实验指派列表
     */
    private Map<Long, List<String>> assignments;
}
