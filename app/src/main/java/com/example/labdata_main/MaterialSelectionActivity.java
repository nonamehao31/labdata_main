package com.example.labdata_main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.dao.MaterialDao;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.MixDesign;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaterialSelectionActivity extends AppCompatActivity {
    private TabLayout materialTypeTabs;
    private TextView materialName;
    private TextView materialBatch;
    private ChipGroup gradationChips;
    private Slider percentageSlider;
    private TextView percentageText;
    private float maxAvailablePercentage = 100f;

    private LinearLayout materialPropertyContainer;
    private CardView propertyCard1, propertyCard2;
    private TextView propertyText1, propertyText2;
    private ImageView propertyCheck1, propertyCheck2;

    private MaterialDao materialDao;
    private String selectedMaterialType;
    private String selectedGradation;
    private MaterialProperty selectedProperty;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_selection);

        // 初始化数据库
        materialDao = AppDatabase.getInstance(this).materialDao();

        // 获取可用百分比
        maxAvailablePercentage = getIntent().getFloatExtra("maxPercentage", 100f);

        initViews();
        setupTabs();
        setupGradationChips();
        setupPercentageSlider();
        setupListeners();

        // 默认选择第一个标签
        updateMaterialInfo(0);
    }

    private void initViews() {
        materialTypeTabs = findViewById(R.id.material_type_tabs);
        materialName = findViewById(R.id.material_name);
        materialBatch = findViewById(R.id.material_batch);
        gradationChips = findViewById(R.id.gradation_chips);
        percentageSlider = findViewById(R.id.percentage_slider);
        percentageText = findViewById(R.id.percentage_text);
        
        materialPropertyContainer = findViewById(R.id.material_property_container);
        propertyCard1 = findViewById(R.id.property_card_1);
        propertyCard2 = findViewById(R.id.property_card_2);
        propertyText1 = findViewById(R.id.property_text_1);
        propertyText2 = findViewById(R.id.property_text_2);
        propertyCheck1 = findViewById(R.id.property_check_1);
        propertyCheck2 = findViewById(R.id.property_check_2);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 设置最大可用百分比
        percentageSlider.setValueTo(maxAvailablePercentage);
        updatePercentageText(0f);
    }

    private void setupTabs() {
        String[] tabTitles = {"沥青", "沙子", "石子"};
        for (String title : tabTitles) {
            materialTypeTabs.addTab(materialTypeTabs.newTab().setText(title));
        }
    }

    private void setupGradationChips() {
        String[] gradations = {"4.75", "9.5", "13.2", "16", "19", "26.5", "31.5"};
        
        for (String gradation : gradations) {
            Chip chip = new Chip(this);
            chip.setText(gradation + "mm");
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setTextSize(16);
            chip.setChipMinHeight(56);
            chip.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setCheckedIconTint(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            chip.setRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.blue_light)));
            chip.setChipStrokeWidth(1);
            chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.divider)));
            chip.setEnsureMinTouchTargetSize(true);
            
            gradationChips.addView(chip);
        }
    }

    private void setupPercentageSlider() {
        percentageSlider.addOnChangeListener((slider, value, fromUser) -> {
            updatePercentageText(value);
        });
        
        percentageSlider.setValue(0);
    }

    private void setupListeners() {
        materialTypeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateMaterialInfo(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        gradationChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                selectedGradation = chip.getText().toString();
            } else {
                selectedGradation = null;
            }
        });

        findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (validateSelection()) {
                saveAndReturn();
            }
        });
    }

    private boolean validateSelection() {
        if (selectedProperty == null) {
            Toast.makeText(this, "请选择材料属性", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedMaterialType.equals("asphalt")) {
            selectedGradation = "";
        } else if (selectedGradation == null) {
            Toast.makeText(this, "请选择级配", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (percentageSlider.getValue() == 0) {
            Toast.makeText(this, "请设置配比百分比", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateMaterialInfo(int position) {
        // 清除之前的选择状态
        clearPropertySelection();
        materialName.setText("");
        materialBatch.setText("");
        selectedProperty = null;
        selectedGradation = null;
        
        switch (position) {
            case 0:
                selectedMaterialType = "asphalt";
                materialBatch.setText("请选择沥青类型");
                updateGradationVisibility(false);
                break;
            case 1:
                selectedMaterialType = "sand";
                updateGradationVisibility(true);
                break;
            case 2:
                selectedMaterialType = "stone";
                materialBatch.setText("请选择石子类型");
                updateGradationVisibility(true);
                break;
        }

        // 从数据库加载材料属性
        executorService.execute(() -> {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
            List<MaterialProperty> properties = databaseHelper.getMaterialPropertiesByType(selectedMaterialType);
            mainHandler.post(() -> {
                if (properties.size() > 0) {
                    if (selectedMaterialType.equals("sand")) {
                        // 沙子只有一种属性，直接选中
                        selectedProperty = properties.get(0);
                        materialName.setText(selectedProperty.name);
                        materialBatch.setText("编号：" + selectedProperty.code);
                        materialPropertyContainer.setVisibility(View.GONE);
                    } else {
                        setupPropertyCards(properties);
                    }
                }
            });
        });
    }

    private void updateGradationVisibility(boolean show) {
        findViewById(R.id.gradation_title).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.gradation_scroll).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void clearPropertySelection() {
        materialPropertyContainer.setVisibility(View.VISIBLE);
        propertyCheck1.setVisibility(View.GONE);
        propertyCheck2.setVisibility(View.GONE);
        propertyCard1.setCardElevation(2f);
        propertyCard2.setCardElevation(2f);
    }

    private void setupPropertyCards(List<MaterialProperty> properties) {
        if (properties.size() >= 1) {
            MaterialProperty prop1 = properties.get(0);
            propertyText1.setText(prop1.name);
            propertyCard1.setOnClickListener(v -> selectProperty(true, prop1));
        }

        if (properties.size() >= 2) {
            MaterialProperty prop2 = properties.get(1);
            propertyText2.setText(prop2.name);
            propertyCard2.setOnClickListener(v -> selectProperty(false, prop2));
        }
    }

    private void selectProperty(boolean isFirst, MaterialProperty property) {
        selectedProperty = property;
        
        // 更新选中状态
        propertyCheck1.setVisibility(isFirst ? View.VISIBLE : View.GONE);
        propertyCheck2.setVisibility(isFirst ? View.GONE : View.VISIBLE);
        
        // 更新卡片阴影
        propertyCard1.setCardElevation(isFirst ? 8f : 2f);
        propertyCard2.setCardElevation(isFirst ? 2f : 8f);
        
        // 更新材料信息
        materialName.setText(property.name);
        materialBatch.setText("编号：" + property.code);
    }

    private void updatePercentageText(float value) {
        String formattedValue = String.format("%.1f%%", value);
        percentageText.setText(formattedValue);
    }

    private void saveAndReturn() {
        executorService.execute(() -> {
            try {
                // 获取最新的设计组号
                int newGroup = materialDao.getLatestDesignGroup() + 1;

                // 创建混合设计
                MixDesign design = new MixDesign();
                design.setDesignGroup(newGroup);
                design.setMaterialType(selectedMaterialType);
                design.setMaterialName(selectedProperty.name);
                design.setMaterialCode(selectedProperty.code);
                design.setGradation(selectedGradation);
                design.setPercentage(percentageSlider.getValue());
                design.setTimestamp(System.currentTimeMillis());
                design.setProportion(percentageSlider.getValue() / 100.0);

                // 插入混合设计
                materialDao.insertMixDesign(design);

                // 在主线程更新UI
                mainHandler.post(() -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("materialType", selectedMaterialType);
                    resultIntent.putExtra("materialName", selectedProperty.name);
                    resultIntent.putExtra("materialCode", selectedProperty.code);
                    resultIntent.putExtra("designGroup", design.getDesignGroup());
                    resultIntent.putExtra("gradation", selectedGradation);
                    resultIntent.putExtra("percentage", percentageSlider.getValue());

                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            } catch (Exception e) {
                // 在主线程显示错误
                mainHandler.post(() -> {
                    Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
