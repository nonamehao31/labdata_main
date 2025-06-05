package com.example.labdata_main.model;

/**
 * 用户数据模型类
 * 用于存储用户的基本信息，包括单位、姓名、电话、邮箱等
 */
public class User {
    // 用户ID，数据库主键
    private int id;
    // 用户所属单位
    private String company;
    // 用户姓名
    private String name;
    // 用户电话
    private String phone;
    // 用户邮箱，用作登录账号
    private String email;
    // 用户密码
    private String password;
    // 用户类型：0表示实验员，1表示管理员
    private int userType;
    // 用户权限：是否允许添加混合料实验
    private boolean allowAddMixture;
    // 用户权限：是否允许添加沥青实验
    private boolean allowAddAsphalt;
    // 用户权限：是否允许添加配合比
    private boolean allowAddMixratio;
    // 用户权限：是否允许设备初始化
    private boolean allowDeviceInit;
    
    // 默认构造函数
    public User() {
    }

    /**
     * 带参数的构造函数
     * @param company 单位名称
     * @param name 用户姓名
     * @param phone 电话号码
     * @param email 邮箱地址
     * @param password 登录密码
     * @param userType 用户类型（0：实验员，1：管理员）
     */
    public User(String company, String name, String phone, String email, String password, int userType) {
        this.company = company;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;    
    }
    
    public boolean isAllowAddMixture() {
        return allowAddMixture;
    }

    public void setAllowAddMixture(boolean allowAddMixture) {
        this.allowAddMixture = allowAddMixture;
    }

    public boolean isAllowAddAsphalt() {
        return allowAddAsphalt;
    }

    public void setAllowAddAsphalt(boolean allowAddAsphalt) {
        this.allowAddAsphalt = allowAddAsphalt;
    }

    public boolean isAllowAddMixratio() {
        return allowAddMixratio;
    }

    public void setAllowAddMixratio(boolean allowAddMixratio) {
        this.allowAddMixratio = allowAddMixratio;
    }
    
    public boolean isAllowDeviceInit() {
        return allowDeviceInit;
    }

    public void setAllowDeviceInit(boolean allowDeviceInit) {
        this.allowDeviceInit = allowDeviceInit;
    }
    
    /**
     * 判断用户是否为管理员
     * @return 如果是管理员则返回true，否则返回false
     */
    public boolean isAdmin() {
        return userType == 1;
    }
}
