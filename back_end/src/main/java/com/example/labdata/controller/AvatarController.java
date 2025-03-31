package com.example.labdata.controller;

import com.example.labdata.model.User;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AvatarController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    // 上传用户头像
    @PostMapping("/users/{userId}/avatar")
    public ApiResponse<String> uploadAvatar(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return new ApiResponse<>(false, "用户不存在", null);
            }

            User user = userOptional.get();
            
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名
            String filename = "user_" + userId + "_" + System.currentTimeMillis() + getFileExtension(file.getOriginalFilename());
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 更新用户头像字段
            user.setAvatarPath(filename);
            userRepository.save(user);
            
            System.out.println("头像已上传: " + filePath.toString());
            
            return new ApiResponse<>(true, "头像上传成功", filename);
        } catch (IOException e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "头像上传失败: " + e.getMessage(), null);
        }
    }

    // 获取用户头像
    @GetMapping("/users/{userId}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent() || userOptional.get().getAvatarPath() == null) {
                return ResponseEntity.notFound().build();
            }

            User user = userOptional.get();
            String avatarPath = user.getAvatarPath();
            
            Path filePath = Paths.get(uploadDir, "avatars", avatarPath);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + avatarPath + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // 辅助方法：获取文件扩展名
    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? ".jpg" : filename.substring(dotIndex);
    }
}
