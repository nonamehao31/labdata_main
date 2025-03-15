package com.example.labdata.repository;

import com.example.labdata.model.MixtureTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixtureTaskRepository extends JpaRepository<MixtureTask, Long> {
    // 通过类型查找所有任务
    List<MixtureTask> findByTaskType(String taskType);
}
