package com.example.labdata.repository;

import com.example.labdata.model.Specimen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Long> {
    
    /**
     * 根据配比ID查找所有试件
     * @param mixRatioId 配比ID
     * @return 试件列表
     */
    List<Specimen> findByMixRatioId(Long mixRatioId);
    
    /**
     * 根据配比ID和压实方法查找试件
     * @param mixRatioId 配比ID
     * @param compactionMethod 压实方法
     * @return 试件列表
     */
    List<Specimen> findByMixRatioIdAndCompactionMethod(Long mixRatioId, String compactionMethod);
    
    /**
     * 根据单位ID查找试件
     * @param specimenCompany 单位ID（将自动转换为字符串）
     * @return 试件列表
     */
    @Query(value = "SELECT * FROM specimens s WHERE s.specimen_company = CAST(:specimenCompany AS VARCHAR)", nativeQuery = true)
    List<Specimen> findBySpecimenCompanyWithCast(@Param("specimenCompany") Long specimenCompany);
    
    /**
     * 根据单位ID查询不同的压实方法（去重）
     * @param specimenCompany 单位ID（将自动转换为字符串）
     * @return 压实方法列表
     */
    @Query(value = "SELECT DISTINCT s.compaction_method FROM specimens s WHERE s.specimen_company = CAST(:specimenCompany AS VARCHAR)", nativeQuery = true)
    List<String> findDistinctCompactionMethodsBySpecimenCompany(@Param("specimenCompany") Long specimenCompany);
    
    /**
     * 根据单位ID和压实方法查询压实方法详情（包含拌合参数）
     * @param specimenCompany 单位ID
     * @param compactionMethod 压实方法名称
     * @return 压实方法详情
     */
    @Query(value = "SELECT s.* FROM specimens s WHERE s.specimen_company = CAST(:specimenCompany AS VARCHAR) AND s.compaction_method = :compactionMethod ORDER BY s.creation_time DESC LIMIT 1", nativeQuery = true)
    Specimen findLatestSpecimenByCompanyAndMethod(@Param("specimenCompany") Long specimenCompany, @Param("compactionMethod") String compactionMethod);
    
    /**
     * 根据ID查找试件
     * @param id 试件ID
     * @return 试件对象
     */
    Optional<Specimen> findById(Long id);
}
