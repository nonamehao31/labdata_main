package com.example.labdata_main;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ExperimentDesignActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvProjectName;
    private TextView tvProgress;
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_task_setup);

        projectName = getIntent().getStringExtra("project_name");
        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProgress = findViewById(R.id.tvProgress);
    }

    private void updateUI() {
        tvProjectName.setText(projectName);
        // 设置进度文字中"实验定制"为紫色
        int purpleColor = ContextCompat.getColor(this, android.R.color.holo_purple);
        tvProgress.setTextColor(purpleColor);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
