package com.example.labdata.repository;

import com.example.labdata.model.SupportMixtureTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMixtureTaskRepository extends JpaRepository<SupportMixtureTask, Integer> {
    // 通过类型查找所有支持的任务
    List<SupportMixtureTask> findByTaskType(String taskType);
}
