package com.example.labdata.repository;

import com.example.labdata.model.TestAsphaltMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 沥青材料仓库接口
 */
@Repository
public interface TestAsphaltMaterialRepository extends JpaRepository<TestAsphaltMaterial, Long> {
    
    /**
     * 根据组织ID查询所有沥青材料
     * @param organizationId 组织ID
     * @return 沥青材料列表
     */
    List<TestAsphaltMaterial> findByOrganizationId(Long organizationId);
    
    /**
     * 根据组织ID和沥青类型查询沥青材料
     * @param organizationId 组织ID
     * @param asphaltCatalog 沥青类型
     * @return 沥青材料列表
     */
    List<TestAsphaltMaterial> findByOrganizationIdAndAsphaltCatalog(Long organizationId, String asphaltCatalog);
    
    /**
     * 根据组织ID查询未过期的沥青材料
     * @param organizationId 组织ID
     * @param currentDate 当前日期
     * @return 沥青材料列表
     */
    @Query("SELECT a FROM TestAsphaltMaterial a WHERE a.organizationId = :organizationId AND a.asphaltTestDue >= :currentDate")
    List<TestAsphaltMaterial> findActiveByOrganizationId(@Param("organizationId") Long organizationId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * 根据沥青标号和组织ID查询沥青材料
     * @param asphaltGrade 沥青标号
     * @param organizationId 组织ID
     * @return 沥青材料列表
     */
    List<TestAsphaltMaterial> findByAsphaltGradeAndOrganizationId(String asphaltGrade, Long organizationId);
}
