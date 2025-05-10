package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ManufacturingMethodActivity extends AppCompatActivity 
        implements SpecimenProcessBottomSheet.OnProcessConfirmedListener {
    private ImageButton btnBack;
    private ImageButton btnAddSpecimen;
    private Button btnNext;
    private TextView tvProjectName;
    private TextView tvProgress;
    private String projectName;
    private String selectedMixing;
    private String selectedCompaction;
    private String selectedCutting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manufacturing_method);

        projectName = getIntent().getStringExtra("project_name");
        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddSpecimen = findViewById(R.id.btnAddSpecimen);
        btnNext = findViewById(R.id.btnNext);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProgress = findViewById(R.id.tvProgress);
    }

    private void updateUI() {
        tvProjectName.setText(projectName);
        // 设置进度文字中"制件方式"为蓝色
        int blueColor = ContextCompat.getColor(this, R.color.blue);
        tvProgress.setTextColor(blueColor);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnAddSpecimen.setOnClickListener(v -> {
            SpecimenProcessBottomSheet bottomSheet = new SpecimenProcessBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "SpecimenProcess");
        });

        btnNext.setOnClickListener(v -> {
            // 跳转到下一个界面
            Intent intent = new Intent(this, ExperimentDesignActivity.class);
            intent.putExtra("project_name", projectName);
            startActivity(intent);
        });
    }

    public void enableNextButton() {
        btnNext.setEnabled(true);
    }

    @Override
    public void onProcessConfirmed(String mixing, String compaction, String cutting) {
        this.selectedMixing = mixing;
        this.selectedCompaction = compaction;
        this.selectedCutting = cutting;
        btnNext.setEnabled(true);
    }
}
