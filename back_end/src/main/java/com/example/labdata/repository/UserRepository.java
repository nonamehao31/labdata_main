package com.example.labdata.repository;

import com.example.labdata.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByIdIn(List<Long> userIds);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    
    // 根据组织名称查找用户
    Optional<User> findFirstByOrganization(String organization);
    
    // 根据组织ID查找所有用户
    List<User> findByOrganizationId(Long organizationId);
}
