package com.example.labdata.repository;

import com.example.labdata.model.AsphaltTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 沥青实验任务数据访问接口
 */
@Repository
public interface AsphaltTaskRepository extends JpaRepository<AsphaltTask, Long> {
    
    /**
     * 根据实验类型查找沥青实验任务
     * 
     * @param asphaltExperimentType 实验类型
     * @return 沥青实验任务列表
     */
    List<AsphaltTask> findByAsphaltExperimentType(String asphaltExperimentType);
    
    /**
     * 根据实验名称查找沥青实验任务
     * 
     * @param asphaltExperimentName 实验名称
     * @return 沥青实验任务列表
     */
    List<AsphaltTask> findByAsphaltExperimentName(String asphaltExperimentName);
}
