package com.example.labdata.repository;

import com.example.labdata.model.CompactionMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 制件方法数据访问接口
 */
@Repository
public interface CompactionMethodRepository extends JpaRepository<CompactionMethod, Long> {
    
    /**
     * 根据单位名称查询制件方法列表
     * @param organization 单位名称
     * @return 制件方法列表
     */
    List<CompactionMethod> findByOrganization(String organization);
    
    /**
     * 查询特定单位的默认制件方法
     * @param organization 单位名称
     * @param isDefault 是否是默认方法
     * @return 默认制件方法列表
     */
    List<CompactionMethod> findByOrganizationAndIsDefault(String organization, Boolean isDefault);
}
