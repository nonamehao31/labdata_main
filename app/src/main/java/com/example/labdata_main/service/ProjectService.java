package com.example.labdata_main.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.ProjectResponse;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 项目服务类，处理项目相关的业务逻辑
 */
public class ProjectService {
    private static final String TAG = "ProjectService";
    private final ApiService apiService;
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final PreferenceManager preferenceManager;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public ProjectService(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.databaseHelper = new DatabaseHelper(context);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * 从服务器获取项目列表
     *
     * @param callback 回调接口，用于返回项目列表
     */
    public void getProjects(ProjectCallback callback) {
        // 获取当前用户的公司ID，如果没有则使用默认值
        String companyId = preferenceManager.getCompanyId();
        // TODO: 使用companyId来获取项目列表
        
        Call<ApiResponse<List<ProjectResponse>>> call = apiService.getProjects();
        call.enqueue(new Callback<ApiResponse<List<ProjectResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProjectResponse>>> call, Response<ApiResponse<List<ProjectResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ProjectResponse> projectResponses = response.body().getData();
                    List<Project> projects = convertToLocalProjects(projectResponses);
                    callback.onProjectsLoaded(projects);
                    
                    // 同步到本地数据库
                    syncProjectsToLocalDb(projects);
                } else {
                    // API调用失败，尝试从本地数据库加载
                    Log.e(TAG, "API调用失败，从本地数据库加载项目");
                    loadProjectsFromLocalDb(callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProjectResponse>>> call, Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage());
                // 网络请求失败，从本地数据库加载
                loadProjectsFromLocalDb(callback);
            }
        });
    }

    /**
     * 从本地数据库加载项目
     *
     * @param callback 回调接口
     */
    private void loadProjectsFromLocalDb(ProjectCallback callback) {
        new Thread(() -> {
            List<Project> projects = databaseHelper.getAllProjects();
            if (callback != null) {
                callback.onProjectsLoaded(projects);
            }
        }).start();
    }

    /**
     * 将服务器返回的项目同步到本地数据库
     *
     * @param projects 项目列表
     */
    private void syncProjectsToLocalDb(List<Project> projects) {
        new Thread(() -> {
            // 先清空本地项目表
            databaseHelper.clearProjects();
            
            // 批量插入新项目
            for (Project project : projects) {
                databaseHelper.insertProject(project);
            }
            Log.d(TAG, "项目同步到本地数据库完成");
        }).start();
    }

    /**
     * 删除项目
     *
     * @param project 要删除的项目
     * @param callback 删除结果回调
     */
    public void deleteProject(Project project, DeleteProjectCallback callback) {
        if (project == null || project.getId() <= 0) {
            callback.onDeleteResult(false, "无效的项目");
            return;
        }

        // 调用API删除项目
        Call<ApiResponse<Boolean>> call = apiService.deleteProject(Long.valueOf(project.getId()));
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 删除成功，从本地数据库中删除
                    new Thread(() -> {
                        databaseHelper.deleteProject(project.getId());
                        Log.d(TAG, "项目已从本地数据库删除: " + project.getName());
                    }).start();
                    
                    callback.onDeleteResult(true, "项目删除成功");
                } else {
                    String errorMessage = "删除失败: ";
                    if (response.body() != null) {
                        errorMessage += response.body().getMessage();
                    } else {
                        errorMessage += "服务器响应错误";
                    }
                    Log.e(TAG, errorMessage);
                    callback.onDeleteResult(false, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                String errorMessage = "网络请求失败: " + t.getMessage();
                Log.e(TAG, errorMessage);
                callback.onDeleteResult(false, errorMessage);
            }
        });
    }

    /**
     * 将服务器响应转换为本地项目对象
     *
     * @param projectResponses 服务器项目响应列表
     * @return 本地项目对象列表
     */
    private List<Project> convertToLocalProjects(List<ProjectResponse> projectResponses) {
        List<Project> projects = new ArrayList<>();
        if (projectResponses != null) {
            for (ProjectResponse response : projectResponses) {
                Project project = new Project();
                project.setId(response.getId().intValue());
                project.setName(response.getName());
                project.setDeadline(response.getDeadline());
                
                // 设置创建时间，如果服务器返回的是格式化时间字符串，需要转换
                if (response.getCreatedDate() != null) {
                    project.setCreateTime(response.getCreatedDate().getTime());
                }
                
                // 设置访问权限
                project.setAccessible(true);
                project.setHasAccess(true);
                
                projects.add(project);
            }
        }
        return projects;
    }

    /**
     * 项目回调接口
     */
    public interface ProjectCallback {
        void onProjectsLoaded(List<Project> projects);
    }
    
    /**
     * 删除项目回调接口
     */
    public interface DeleteProjectCallback {
        void onDeleteResult(boolean success, String message);
    }
}
