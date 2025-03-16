package com.example.labdata.config;

import com.example.labdata.security.CustomUserDetailsService;
import com.example.labdata.security.JwtAuthenticationEntryPoint;
import com.example.labdata.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 配置HTTP安全性
        http
            .cors().and()                       // 启用CORS支持
            .csrf().disable()                   // 禁用CSRF保护（在使用JWT时不需要）
            .exceptionHandling()                // 配置异常处理
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()                // 配置会话管理
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();
        
        // 配置请求授权规则
        http.authorizeHttpRequests(requests -> requests
                // 公开端点 - 允许所有用户访问
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/user/checkUsernameAvailability").permitAll()
                .requestMatchers("/user/checkEmailAvailability").permitAll()
                
                // 静态资源 - 允许所有用户访问
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                
                // 项目API - 允许公开访问项目名称
                .requestMatchers("/api/projects/*/name").permitAll()
                
                // 用户资料 - 允许GET请求访问
                .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                
                // 设备API - 需要认证才能访问
                .requestMatchers("/devices/**").authenticated()
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
        );

        // 添加自定义过滤器和认证提供者
        http.authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
