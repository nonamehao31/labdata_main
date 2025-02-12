package com.example.labdata_main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.adapter.ProjectSelectionAdapter;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import java.util.ArrayList;
import java.util.List;

public class SelectProjectActivity extends AppCompatActivity implements ProjectSelectionAdapter.OnProjectSelectedListener {
    private RecyclerView rvProjects;
    private MaterialButton btnNext;
    private Project selectedProject;
    private ProjectSelectionAdapter adapter;
    private DatabaseHelper databaseHelper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_select_project);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        loadProjects();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvProjects = findViewById(R.id.rvProjects);
        btnNext = findViewById(R.id.btnNext);

        // 设置RecyclerView
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectSelectionAdapter(this);
        rvProjects.setAdapter(adapter);

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            if (selectedProject == null) {
                Toast.makeText(this, "请选择一个项目", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到选择配比界面
            Intent intent = new Intent(this, ExperimentTaskSetupActivity.class);
            intent.putExtra("project_id", selectedProject.getId());
            intent.putExtra("project_name", selectedProject.getName());
            startActivity(intent);
        });
    }

    private void loadProjects() {
        new Thread(() -> {
            List<Project> projects = databaseHelper.getAllProjects();
            mainHandler.post(() -> adapter.submitList(new ArrayList<>(projects)));
        }).start();
    }

    @Override
    public void onProjectSelected(Project project) {
        this.selectedProject = project;
        btnNext.setEnabled(true);
    }

    @Override
    public void onProjectDeleteRequested(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("删除项目")
                .setMessage("确定要删除项目 \"" + project.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    new Thread(() -> {
                        databaseHelper.deleteProject(project.getId());
                        mainHandler.post(this::loadProjects);
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
