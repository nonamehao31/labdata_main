package com.example.labdata.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 简单的数据库连接测试类
 * 可以在应用程序启动前独立运行，检查数据库连接是否可用
 */
public class DatabaseConnectionTest {
    
    private static final String URL = "jdbc:postgresql://127.0.0.1:5432/labdata";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";
    
    /**
     * 测试数据库连接是否可用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("=== 测试数据库连接 ===");
        System.out.println("URL: " + URL);
        System.out.println("User: " + USER);
        
        try {
            // 注册PostgreSQL驱动
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC驱动已注册");
            
            // 尝试建立连接
            System.out.println("尝试连接数据库...");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            if (connection != null) {
                System.out.println("数据库连接成功!");
                connection.close();
            } else {
                System.out.println("数据库连接失败!");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("找不到PostgreSQL JDBC驱动: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("数据库连接错误: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("根本原因: " + e.getCause().getMessage());
            }
            // 打印堆栈便于调试
            e.printStackTrace();
        }
    }
}
