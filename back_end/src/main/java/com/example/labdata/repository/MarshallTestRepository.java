package com.example.labdata.repository;

import com.example.labdata.model.MarshallTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 马歇尔试验数据仓库接口
 */
@Repository
public interface MarshallTestRepository extends JpaRepository<MarshallTest, Long> {
    
    /**
     * 根据任务ID查找马歇尔试验数据
     *
     * @param taskId 任务ID
     * @return 马歇尔试验数据列表
     */
    List<MarshallTest> findByTaskId(String taskId);
    
    /**
     * 原生SQL查询，避免潜在的类型转换问题
     *
     * @param taskId 任务ID
     * @return 马歇尔试验数据列表
     */
    @Query(value = "SELECT * FROM marshall_test WHERE task_id LIKE :taskId%", nativeQuery = true)
    List<MarshallTest> findAllByTaskIdNative(@Param("taskId") String taskId);
}
