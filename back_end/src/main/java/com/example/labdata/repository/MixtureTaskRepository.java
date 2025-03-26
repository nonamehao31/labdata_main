package com.example.labdata.repository;

import com.example.labdata.model.MixtureTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MixtureTaskRepository extends JpaRepository<MixtureTask, String> {
    // 通过类型查找所有任务
    List<MixtureTask> findByTaskType(String taskType);
    
    // 通过任务ID前缀查找任务 (使用JOIN FETCH加载关联的project实体)
    @Query("SELECT DISTINCT mt FROM MixtureTask mt LEFT JOIN FETCH mt.project WHERE mt.taskId LIKE CONCAT(:taskIdPrefix, '%')")
    Optional<MixtureTask> findByTaskIdPrefix(@Param("taskIdPrefix") String taskIdPrefix);
    
    // 通过任务ID前缀查找多个任务
    @Query("SELECT mt FROM MixtureTask mt WHERE mt.taskId LIKE CONCAT(:taskIdPrefix, '%')")
    List<MixtureTask> findAllByTaskIdPrefix(@Param("taskIdPrefix") String taskIdPrefix);
    
    // 使用原生SQL查询通过任务ID前缀查找任务信息
    @Query(value = 
           "SELECT task_id, task_name, task_type, prepare_status, making_status, testing_status, " +
           "assigned_mixing_equipment, assigned_forming_equipment, assigned_testing_equipment, " +
           "mixing_equipment_manufacturer, forming_equipment_manufacturer, testing_equipment_manufacturer " +
           "FROM mixture_task WHERE task_id LIKE :taskIdPrefix || '%'", 
           nativeQuery = true)
    List<Map<String, Object>> findAllByTaskIdPrefixNative(@Param("taskIdPrefix") String taskIdPrefix);

    //查询任务指派名称
    @Query(value = "SELECT task_assignment FROM mixture_task WHERE task_id = :taskId", 
           nativeQuery = true)
    String findTaskAssignmentById(@Param("taskId") String taskId);
    
    // 通过任务ID精确查找任务
    Optional<MixtureTask> findByTaskId(String taskId);
    
    // 更新指定任务ID前缀的making_status字段
    @Modifying
    @Transactional
    @Query(value = "UPDATE mixture_task SET making_status = :status WHERE task_id LIKE :taskIdPrefix || '-%' OR task_id = :taskIdPrefix", 
           nativeQuery = true)
    int updateMakingStatusByTaskIdPrefix(@Param("taskIdPrefix") String taskIdPrefix, @Param("status") String status);
}
