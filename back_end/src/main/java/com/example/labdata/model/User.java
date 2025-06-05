package com.example.labdata.model;

import com.example.labdata.model.audit.DateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
@Data
@NoArgsConstructor
public class User extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
    private String name;

    @NotBlank
    @Size(max = 15)
    private String username;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(max = 100)
    private String password;
    
    // Additional fields from Android app's User model
    private String phone;
    private String organization;
    private Long organizationId; 
    
    @Column(name = "admin", nullable = false, columnDefinition = "boolean default false")
    private boolean admin;

    public boolean isAdmin() {
        System.out.println("Getting admin value: " + this.admin + " for user: " + this.username);
        return admin;
    }
    
    @Column(name = "allow_add_mixture", nullable = false, columnDefinition = "boolean default false")
    private boolean allowAddMixture;
    
    @Column(name = "allow_add_asphalt", nullable = false, columnDefinition = "boolean default false")
    private boolean allowAddAsphalt;
    
    @Column(name = "allow_add_mixratio", nullable = false, columnDefinition = "boolean default false")
    private boolean allowAddMixratio;
    
    @Column(name = "allow_device_init", nullable = false, columnDefinition = "boolean default false")
    private boolean allowDeviceInit;
    
    @Column(name = "avatar_path")
    private String avatarPath;
}
