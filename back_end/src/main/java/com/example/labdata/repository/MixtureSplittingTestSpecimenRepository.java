package com.example.labdata.repository;

import com.example.labdata.model.MixtureSplittingTestSpecimen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MixtureSplittingTestSpecimenRepository extends JpaRepository<MixtureSplittingTestSpecimen, String> {

    /**
     * 根据测试ID查找所有试件
     *
     * @param testId 测试ID
     * @return 试件列表
     */
    List<MixtureSplittingTestSpecimen> findByTestId(String testId);

    /**
     * 根据测试ID和试件编号查找试件
     *
     * @param testId 测试ID
     * @param specimenNumber 试件编号
     * @return 试件对象
     */
    Optional<MixtureSplittingTestSpecimen> findByTestIdAndSpecimenNumber(String testId, Integer specimenNumber);

    /**
     * 根据测试ID查找所有试件
     * 使用原生SQL查询以避免潜在的ID转换问题
     *
     * @param testId 测试ID
     * @return 试件列表
     */
    @Query(value = "SELECT * FROM mixture_splitting_test_specimen WHERE test_id = :testId ORDER BY specimen_number", nativeQuery = true)
    List<MixtureSplittingTestSpecimen> findByTestIdNative(@Param("testId") String testId);

    /**
     * 根据试件ID查找单个试件
     * 使用原生SQL查询以避免潜在的ID转换问题
     *
     * @param specimenId 试件ID
     * @return 试件对象
     */
    @Query(value = "SELECT * FROM mixture_splitting_test_specimen WHERE specimen_id = :specimenId", nativeQuery = true)
    Optional<MixtureSplittingTestSpecimen> findBySpecimenIdNative(@Param("specimenId") String specimenId);

    /**
     * 删除指定测试ID的所有试件
     *
     * @param testId 测试ID
     */
    void deleteByTestId(String testId);

    /**
     * 计算指定测试ID的试件数量
     *
     * @param testId 测试ID
     * @return 试件数量
     */
    long countByTestId(String testId);
}
