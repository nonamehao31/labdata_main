package com.example.labdata.repository;

import com.example.labdata.model.MixtureSplittingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MixtureSplittingTestRepository extends JpaRepository<MixtureSplittingTest, String> {

    /**
     * 根据任务ID查找所有劈裂试验测试
     *
     * @param taskId 任务ID
     * @return 测试列表
     */
    List<MixtureSplittingTest> findByTaskId(String taskId);

    /**
     * 根据配比ID查找所有劈裂试验测试
     *
     * @param mixRatioId 配比ID
     * @return 测试列表
     */
    List<MixtureSplittingTest> findByMixRatioId(String mixRatioId);

    /**
     * 根据任务ID和配比ID查找劈裂试验测试
     *
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 测试列表
     */
    List<MixtureSplittingTest> findByTaskIdAndMixRatioId(String taskId, String mixRatioId);

    /**
     * 根据测试ID查找单个劈裂试验测试
     * 使用原生SQL查询以避免潜在的ID转换问题
     *
     * @param testId 测试ID
     * @return 测试对象
     */
    @Query(value = "SELECT * FROM mixture_splitting_test WHERE test_id = :testId", nativeQuery = true)
    Optional<MixtureSplittingTest> findByTestIdNative(@Param("testId") String testId);

    /**
     * 根据任务ID查找所有劈裂试验测试
     * 使用原生SQL查询以避免潜在的ID转换问题
     *
     * @param taskId 任务ID
     * @return 测试列表
     */
    @Query(value = "SELECT * FROM mixture_splitting_test WHERE task_id = :taskId", nativeQuery = true)
    List<MixtureSplittingTest> findByTaskIdNative(@Param("taskId") String taskId);

    /**
     * 检查指定任务和配比的劈裂试验测试是否存在
     *
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 存在返回true，否则返回false
     */
    boolean existsByTaskIdAndMixRatioId(String taskId, String mixRatioId);
}
