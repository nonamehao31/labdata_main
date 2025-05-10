package com.example.labdata_main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.adapter.ProjectAdapter;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import java.util.ArrayList;
import java.util.List;

public class AssignTaskActivity extends AppCompatActivity implements ProjectAdapter.OnProjectClickListener {
    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private DatabaseHelper databaseHelper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<Project> projects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_task);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadProjects();
    }

    private void initViews() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("分配任务");
        }

        // 保存按钮
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveProjectAccess());

        // 设置RecyclerView
        rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(this);
        rvProjects.setAdapter(adapter);
    }

    private void loadProjects() {
        new Thread(() -> {
            projects = databaseHelper.getAllProjects();
            mainHandler.post(() -> {
                if (projects.isEmpty()) {
                    // 显示空状态
                    findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
                    rvProjects.setVisibility(View.GONE);
                } else {
                    // 显示项目列表
                    findViewById(R.id.emptyView).setVisibility(View.GONE);
                    rvProjects.setVisibility(View.VISIBLE);
                    adapter.submitList(projects);
                }
            });
        }).start();
    }

    private void saveProjectAccess() {
        new Thread(() -> {
            boolean success = databaseHelper.updateProjectsAccess(projects);
            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "权限设置已保存", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onProjectClick(Project project) {
        // 处理项目点击事件
        Toast.makeText(this, "已选择项目：" + project.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProjectDeleteClick(Project project) {
        // 在这个界面不需要处理删除操作
    }
}
