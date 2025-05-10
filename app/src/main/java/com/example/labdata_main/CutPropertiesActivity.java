package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.labdata_main.model.CutShape;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class CutPropertiesActivity extends AppCompatActivity {
    private ChipGroup chipGroupShape;
    private Chip chipCuboid, chipCylinder, chipHalfCylinder;
    private LinearLayout layoutCuboid, layoutCylinder, layoutHalfCylinder;
    private TextInputEditText etCutCount;
    private TextInputEditText etLength, etWidth, etHeight;
    private TextInputEditText etRadius, etCylinderHeight;
    private TextInputEditText etHalfRadius, etHalfCylinderHeight;
    private MaterialButton btnConfirm;

    private float mixingTemperature;
    private float mixingSpeed;
    private String compactionMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_properties);

        // 获取传递的数据
        mixingTemperature = getIntent().getFloatExtra("mixing_temperature", 0);
        mixingSpeed = getIntent().getFloatExtra("mixing_speed", 0);
        compactionMethod = getIntent().getStringExtra("compaction_method");

        initViews();
        setupListeners();
    }

    private void initViews() {
        // 初始化顶部工具栏
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("切割属性");
        btnBack.setOnClickListener(v -> finish());

        // 初始化切割形状选择
        chipGroupShape = findViewById(R.id.chipGroupCutShape);
        chipCuboid = findViewById(R.id.chipCuboid);
        chipCylinder = findViewById(R.id.chipCylinder);
        chipHalfCylinder = findViewById(R.id.chipHalfCylinder);

        // 初始化布局容器
        layoutCuboid = findViewById(R.id.layoutCuboid);
        layoutCylinder = findViewById(R.id.layoutCylinder);
        layoutHalfCylinder = findViewById(R.id.layoutHalfCylinder);

        // 初始化输入框
        etCutCount = findViewById(R.id.etCutCount);
        etLength = findViewById(R.id.etLength);
        etWidth = findViewById(R.id.etWidth);
        etHeight = findViewById(R.id.etHeight);
        etRadius = findViewById(R.id.etRadius);
        etCylinderHeight = findViewById(R.id.etCylinderHeight);
        etHalfRadius = findViewById(R.id.etHalfRadius);
        etHalfCylinderHeight = findViewById(R.id.etHalfCylinderHeight);

        // 初始化确认按钮
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupListeners() {
        // 切割形状选择监听
        chipGroupShape.setOnCheckedChangeListener((group, checkedId) -> {
            layoutCuboid.setVisibility(View.GONE);
            layoutCylinder.setVisibility(View.GONE);
            layoutHalfCylinder.setVisibility(View.GONE);

            if (checkedId == R.id.chipCuboid) {
                layoutCuboid.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chipCylinder) {
                layoutCylinder.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chipHalfCylinder) {
                layoutHalfCylinder.setVisibility(View.VISIBLE);
            }
        });

        // 确认按钮点击监听
        btnConfirm.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        // 验证切割形状是否选择
        if (chipGroupShape.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "请选择切割形状", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取切割形状
        CutShape cutShape;
        if (chipCuboid.isChecked()) {
            cutShape = CutShape.CUBOID;
        } else if (chipCylinder.isChecked()) {
            cutShape = CutShape.CYLINDER;
        } else {
            cutShape = CutShape.HALF_CYLINDER;
        }

        // 验证切割数量
        String countStr = etCutCount.getText().toString();
        if (countStr.isEmpty() && cutShape != CutShape.HALF_CYLINDER) {
            Toast.makeText(this, "请输入切割数量", Toast.LENGTH_SHORT).show();
            return;
        }
        int cutCount = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);

        // 验证尺寸
        try {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("cut_shape", cutShape.name());
            resultIntent.putExtra("cut_count", cutCount);

            switch (cutShape) {
                case CUBOID:
                    float length = Float.parseFloat(etLength.getText().toString());
                    float width = Float.parseFloat(etWidth.getText().toString());
                    float height = Float.parseFloat(etHeight.getText().toString());
                    resultIntent.putExtra("length", length);
                    resultIntent.putExtra("width", width);
                    resultIntent.putExtra("height", height);
                    break;

                case CYLINDER:
                    float radius = Float.parseFloat(etRadius.getText().toString());
                    float cylinderHeight = Float.parseFloat(etCylinderHeight.getText().toString());
                    resultIntent.putExtra("radius", radius);
                    resultIntent.putExtra("height", cylinderHeight);
                    break;

                case HALF_CYLINDER:
                    float halfRadius = Float.parseFloat(etHalfRadius.getText().toString());
                    float halfCylinderHeight = Float.parseFloat(etHalfCylinderHeight.getText().toString());
                    resultIntent.putExtra("radius", halfRadius);
                    resultIntent.putExtra("height", halfCylinderHeight);
                    break;
            }

            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的尺寸", Toast.LENGTH_SHORT).show();
        }
    }
}
