package com.example.labdata.repository;

import com.example.labdata.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目仓库接口，处理项目数据的存储和查询
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 根据公司ID查找所有项目
     * 
     * @param companyId 公司ID
     * @return 项目列表
     */
    List<Project> findByCompanyId(String companyId);
    
    /**
     * 根据创建者查找所有项目
     * 
     * @param createdBy 创建者ID或用户名
     * @return 项目列表
     */
    List<Project> findByCreatedBy(String createdBy);
    
    /**
     * 根据项目ID查找项目
     * 
     * @param projectId 项目ID
     * @return 项目
     */
    Optional<Project> findByProjectId(String projectId);
}
