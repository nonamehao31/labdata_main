package com.example.labdata.controller;

import com.example.labdata.exception.AppException;
import com.example.labdata.model.User;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.payload.JwtAuthenticationResponse;
import com.example.labdata.payload.LoginRequest;
import com.example.labdata.payload.SignUpRequest;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.security.JwtTokenProvider;
import com.example.labdata.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);
        
        // 获取用户完整信息
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new AppException("找不到用户信息"));
                
        // 返回带有用户真实姓名、公司信息和管理员状态的响应
        return ResponseEntity.ok(new JwtAuthenticationResponse(
            jwt, 
            userPrincipal.getId(), 
            userPrincipal.getUsername(),
            user.getName(), // 添加用户真实姓名
            user.getOrganization(),
            user.getOrganizationId() != null ? user.getOrganizationId().toString() : null,
            user.isAdmin(), // 添加管理员状态
            user.isAllowAddMixture(), // 添加混合料实验权限
            user.isAllowAddAsphalt(), // 添加沥青实验权限
            user.isAllowAddMixratio() // 添加配合比权限
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }
        
        // 查找是否已有同名组织的用户，如果有则共享组织ID
        Long organizationId = getOrCreateOrganizationId(signUpRequest.getOrganization());

        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setPhone(signUpRequest.getPhone());
        user.setOrganization(signUpRequest.getOrganization());
        user.setOrganizationId(organizationId); // 设置组织ID
        
        // 如果前端传递了admin值，使用前端传递的值；否则默认为false
        if (signUpRequest.getAdmin() != null) {
            user.setAdmin(signUpRequest.getAdmin());
            // 记录用户类型设置
            System.out.println("设置用户 " + signUpRequest.getUsername() + " 的管理员状态为: " + signUpRequest.getAdmin());
        } else {
            user.setAdmin(false); // 默认值
            System.out.println("用户 " + signUpRequest.getUsername() + " 未提供管理员状态，使用默认值false");
        }
        
        // 设置初始权限
        // 如果是管理员，默认拥有所有权限；如果是实验员，默认没有任何权限
        boolean hasPermissions = user.isAdmin();
        user.setAllowAddMixture(hasPermissions);
        user.setAllowAddAsphalt(hasPermissions);
        user.setAllowAddMixratio(hasPermissions);

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
    
    /**
     * 获取或创建组织ID
     * @param organizationName 组织名称
     * @return 组织ID
     */
    private Long getOrCreateOrganizationId(String organizationName) {
        if (organizationName == null || organizationName.trim().isEmpty()) {
            // 如果组织名称为空，使用默认ID
            return 10000L;
        }
        
        // 查找是否已有同名组织的用户
        Optional<User> existingUser = userRepository.findFirstByOrganization(organizationName.trim());
        
        if (existingUser.isPresent() && existingUser.get().getOrganizationId() != null) {
            // 如果找到已有用户且组织ID不为空，则复用该ID
            return existingUser.get().getOrganizationId();
        } else {
            // 否则生成新的组织ID
            return generateNewOrganizationId();
        }
    }
    
    /**
     * 生成新的组织ID
     * @return 新的组织ID
     */
    private Long generateNewOrganizationId() {
        // 使用UUID的哈希值生成唯一的组织ID
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }

    /**
     * 检查邮箱是否已注册（路径参数方式）
     * @param email 邮箱地址
     * @return 如果邮箱已经被占用则返回true，否则返回false
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
        boolean exists = userRepository.existsByEmail(email);
        ApiResponse<Boolean> response = new ApiResponse<>(true, null, exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否已注册（查询参数方式）
     * @param email 邮箱地址
     * @return 如果邮箱已经被占用则返回true，否则返回false
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExistsQuery(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        ApiResponse<Boolean> response = new ApiResponse<>(true, null, exists);
        return ResponseEntity.ok(response);
    }

    /**
     * 通过用户名或邮箱获取用户信息
     * @param username 用户名或邮箱
     * @return 包含用户信息的响应实体
     */
    @GetMapping("/user/by-username/{username}")
    public ResponseEntity<?> getUserInfoByUsername(@PathVariable(value = "username") String username) {
        System.out.println("开始查询用户信息: username=" + username);
        
        // 先通过用户名查找
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        // 如果通过用户名找不到，尝试通过邮箱查找
        if (!userOptional.isPresent()) {
            System.out.println("通过用户名找不到用户，尝试通过邮箱查找");
            userOptional = userRepository.findByEmail(username);
        }
        
        if (!userOptional.isPresent()) {
            System.out.println("用户不存在: " + username);
            return ResponseEntity.notFound().build();
        }
        
        User user = userOptional.get();
        
        // 添加调试日志
        System.out.println("用户信息: ID=" + user.getId() + ", 用户名=" + user.getUsername() + 
                          ", 邮箱=" + user.getEmail() + ", 管理员状态=" + user.isAdmin());
        
        try {
            // 直接从数据库再次查询该用户的管理员状态
            String query = "SELECT admin FROM users WHERE id = " + user.getId();
            Object result = entityManager.createNativeQuery(query).getSingleResult();
            System.out.println("数据库中的admin原始值: " + result + " (类型: " + (result != null ? result.getClass().getName() : "null") + ")");
        } catch (Exception e) {
            System.out.println("查询admin字段时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 创建只返回必要信息的响应对象
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("organization", user.getOrganization());
        userInfo.put("organizationId", user.getOrganizationId());
        userInfo.put("admin", user.isAdmin()); // 添加管理员状态
        
        System.out.println("响应中的管理员状态: " + userInfo.get("admin"));
        
        return ResponseEntity.ok(new ApiResponse(true, "用户信息获取成功", userInfo));
    }

    /**
     * 获取同一单位的所有用户信息
     * @param organizationId 单位ID
     * @return 包含用户列表的响应实体
     */
    @GetMapping("/users/by-organization/{organizationId}")
    public ResponseEntity<?> getUsersByOrganization(@PathVariable(value = "organizationId") Long organizationId) {
        List<User> users = userRepository.findByOrganizationId(organizationId);
        
        if (users.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(true, "未找到该单位下的用户", new ArrayList<>()));
        }
        
        // 处理返回数据，移除敏感信息
        List<Map<String, Object>> safeUsers = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> safeUser = new HashMap<>();
            safeUser.put("id", user.getId());
            safeUser.put("username", user.getUsername());
            safeUser.put("name", user.getName());
            safeUser.put("email", user.getEmail());
            safeUser.put("organization", user.getOrganization());
            safeUser.put("phone", user.getPhone());
            safeUser.put("userType", user.isAdmin() ? 1 : 0); // 将admin布尔值转换为整数类型
            safeUser.put("allowAddMixture", user.isAllowAddMixture());
            safeUser.put("allowAddAsphalt", user.isAllowAddAsphalt());
            safeUser.put("allowAddMixratio", user.isAllowAddMixratio());
            
            safeUsers.add(safeUser);
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "获取单位用户成功", safeUsers));
    }

    /**
     * 更新用户权限
     * @param userId 用户ID
     * @param permissions 用户权限参数
     * @return 操作结果
     */
    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<?> updateUserPermissions(
            @PathVariable(value = "userId") Long userId,
            @RequestBody Map<String, Boolean> permissions) {
        
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "找不到指定用户", null));
        }
        
        User user = userOptional.get();
        
        // 只能更新普通用户的权限，管理员默认拥有所有权限
        if (!user.isAdmin()) {
            // 更新权限字段
            if (permissions.containsKey("allowAddMixture")) {
                user.setAllowAddMixture(permissions.get("allowAddMixture"));
            }
            
            if (permissions.containsKey("allowAddAsphalt")) {
                user.setAllowAddAsphalt(permissions.get("allowAddAsphalt"));
            }
            
            if (permissions.containsKey("allowAddMixratio")) {
                user.setAllowAddMixratio(permissions.get("allowAddMixratio"));
            }
            
            if (permissions.containsKey("allowDeviceInit")) {
                user.setAllowDeviceInit(permissions.get("allowDeviceInit"));
            }
            
            userRepository.save(user);
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "用户权限更新成功", true));
    }

    /**
     * 获取用户权限
     * @param userId 用户ID
     * @return 用户权限信息
     */
    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<?> getUserPermissions(
            @PathVariable(value = "userId") Long userId) {
        
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "找不到指定用户", null));
        }
        
        User user = userOptional.get();
        Map<String, Boolean> permissions = new HashMap<>();
        
        // 如果是管理员，返回所有权限为true
        if (user.isAdmin()) {
            permissions.put("allowAddMixture", true);
            permissions.put("allowAddAsphalt", true);
            permissions.put("allowAddMixratio", true);
            permissions.put("allowDeviceInit", true);
        } else {
            // 返回该用户的实际权限
            permissions.put("allowAddMixture", user.isAllowAddMixture());
            permissions.put("allowAddAsphalt", user.isAllowAddAsphalt());
            permissions.put("allowAddMixratio", user.isAllowAddMixratio());
            permissions.put("allowDeviceInit", user.isAllowDeviceInit());
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "获取用户权限成功", permissions));
    }
}
