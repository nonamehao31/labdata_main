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

import jakarta.validation.Valid;
import java.net.URI;
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
                
        // 返回带有公司信息的响应
        return ResponseEntity.ok(new JwtAuthenticationResponse(
            jwt, 
            userPrincipal.getId(), 
            userPrincipal.getUsername(),
            user.getOrganization(),
            user.getOrganizationId() != null ? user.getOrganizationId().toString() : null
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
        user.setAdmin(false); // Default value

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
}
