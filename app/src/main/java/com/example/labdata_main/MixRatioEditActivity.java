package com.example.labdata_main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.example.labdata_main.api.MixRatioApiService;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MixRatioRequest;
import com.example.labdata_main.model.MixRatioResponse;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixRatioEditActivity extends AppCompatActivity {
    private static final String TAG = "MixRatioEditActivity";
    private PieChart pieChart;
    private RecyclerView materialsList;
    private MaterialAdapter adapter;
    private List<MaterialItem> materials;
    private String mixRatioName;
    private TextView mixRatioNameTextView;

    private static final int REQUEST_ADD_MATERIAL = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_ratio_edit);

        // 初始化数据
        materials = new ArrayList<>();
        mixRatioName = getIntent().getStringExtra("mix_ratio_name");
        
        // 初始化视图
        initViews();
        // 设置RecyclerView
        setupRecyclerView();
        // 设置饼图
        setupPieChart();
        // 设置监听器
        setupListeners();
        setupNameEditListener();
    }

    private void initViews() {
        mixRatioNameTextView = findViewById(R.id.mix_ratio_name);
        mixRatioNameTextView.setText(mixRatioName);

        // 初始化返回按钮
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 初始化下一步按钮
        findViewById(R.id.next_step_button).setOnClickListener(v -> {
            if (validateMaterials()) {
                saveMixRatio();
            }
        });

        // 初始化添加材料按钮
        FloatingActionButton addMaterialButton = findViewById(R.id.add_material_button);
        addMaterialButton.setOnClickListener(v -> {
            float availablePercentage = 100f;
            for (MaterialItem item : materials) {
                availablePercentage -= item.getPercentage();
            }

            if (availablePercentage > 0) {
                Intent intent = new Intent(this, MaterialSelectionActivity.class);
                intent.putExtra("maxPercentage", availablePercentage);
                startActivityForResult(intent, REQUEST_ADD_MATERIAL);
            } else {
                showToast("配比总和已达到100%，无法添加新原料");
            }
        });

        pieChart = findViewById(R.id.pie_chart);
        materialsList = findViewById(R.id.materials_list);
    }

    private void setupRecyclerView() {
        adapter = new MaterialAdapter(materials, this::updatePieChart);
        materialsList.setLayoutManager(new LinearLayoutManager(this));
        materialsList.setAdapter(adapter);
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(40f);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        
        // 设置中心文字
        pieChart.setCenterText(mixRatioName);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);
        
        // 设置图例
        pieChart.getLegend().setEnabled(false);
        
        // 初始状态显示空状态
        updatePieChart();
        
        pieChart.animateY(1400);
    }

    private void setupListeners() {
        // 点击添加按钮添加新原料
        FloatingActionButton addButton = findViewById(R.id.add_material_button);
        addButton.setOnClickListener(v -> {
            // 计算剩余可用百分比
            float usedPercentage = 0;
            for (MaterialItem material : materials) {
                usedPercentage += material.getPercentage();
            }
            float availablePercentage = 100 - usedPercentage;

            if (availablePercentage > 0) {
                Intent intent = new Intent(this, MaterialSelectionActivity.class);
                intent.putExtra("maxPercentage", availablePercentage);
                startActivityForResult(intent, REQUEST_ADD_MATERIAL);
            } else {
                showToast("配比总和已达到100%，无法添加新原料");
            }
        });
    }

    private void setupNameEditListener() {
        mixRatioNameTextView.setOnClickListener(v -> showNameEditDialog());
    }

    private void showNameEditDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("编辑配比名称");

        // 创建输入框
        final TextInputLayout inputLayout = new TextInputLayout(this);
        final TextInputEditText input = new TextInputEditText(inputLayout.getContext());
        
        // 配置输入框
        input.setText(mixRatioName);
        input.setHint("请输入配比名称");
        input.setMaxLines(1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        
        // 添加输入验证
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    inputLayout.setError("配比名称不能为空");
                } else if (text.length() > 20) {
                    inputLayout.setError("配比名称不能超过20个字符");
                } else {
                    inputLayout.setError(null);
                }
            }
        });

        // 软键盘完成按钮监听
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty() && newName.length() <= 20) {
                    mixRatioName = newName;
                    mixRatioNameTextView.setText(mixRatioName);
                    updatePieChart();
                    return true;
                }
            }
            return false;
        });

        inputLayout.addView(input);
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        
        builder.setView(inputLayout);

        // 确认和取消按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && newName.length() <= 20) {
                mixRatioName = newName;
                mixRatioNameTextView.setText(mixRatioName);
                updatePieChart(); // 更新饼图标题
            }
        });
        builder.setNegativeButton("取消", null);

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // 弹出软键盘
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });

        dialog.show();
    }

    private boolean validateMaterials() {
        if (materials.isEmpty()) {
            showToast("请添加至少一种原料");
            return false;
        }

        // 检查是否有沥青、沙子和石子
        boolean hasAsphalt = false;
        boolean hasSand = false;
        boolean hasStone = false;
        
        // 检查总百分比
        float totalPercentage = 0f;

        for (MaterialItem material : materials) {
            String type = material.getType();
            if ("asphalt".equals(type)) {
                hasAsphalt = true;
            } else if ("sand".equals(type)) {
                hasSand = true;
            } else if ("stone".equals(type)) {
                hasStone = true;
            }
            totalPercentage += material.getPercentage();
        }

        // 检查必需的原料类型
        StringBuilder missingMaterials = new StringBuilder();
        if (!hasAsphalt) {
            missingMaterials.append("沥青、");
        }
        if (!hasSand) {
            missingMaterials.append("沙子、");
        }
        if (!hasStone) {
            missingMaterials.append("石子、");
        }

        if (missingMaterials.length() > 0) {
            missingMaterials.setLength(missingMaterials.length() - 1); // 移除最后的顿号
            showToast("缺少必需的原料：" + missingMaterials.toString());
            return false;
        }

        // 检查总百分比是否为100%
        if (Math.abs(totalPercentage - 100) > 0.01f) {
            showToast("原料配比总和必须为100%，当前为" + String.format("%.1f%%", totalPercentage));
            return false;
        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MATERIAL && resultCode == RESULT_OK && data != null) {
            // 获取选择的原料信息
            String materialType = data.getStringExtra("materialType");
            String materialName = data.getStringExtra("materialName");
            String materialCode = data.getStringExtra("materialCode");
            String gradation = data.getStringExtra("gradation");
            float percentage = data.getFloatExtra("percentage", 0f);
            // 获取材料ID
            Long materialId = null;
            if (data.hasExtra("materialId")) {
                materialId = data.getLongExtra("materialId", 0L);
                if (materialId == 0L) materialId = null;
            }
            
            Log.d(TAG, "收到材料选择结果 - 类型: " + materialType + ", 名称: " + materialName + 
                  ", 编号: " + materialCode + ", 级配: " + gradation + ", 百分比: " + percentage + 
                  ", 材料ID: " + (materialId != null ? materialId : "无"));

            // 创建原料显示名称
            String displayName;
            if (materialType.equals("sand")) {
                displayName = String.format("%s\n编号：%s\n级配：%s", 
                    materialName, materialCode, gradation);
            } else {
                String typeDisplay = materialType.equals("asphalt") ? "沥青" : "石子";
                displayName = String.format("%s·%s\n编号：%s\n级配：%s", 
                    typeDisplay, materialName, materialCode, gradation);
            }

            // 创建新的原料项
            MaterialItem newMaterial = new MaterialItem(displayName, percentage, materialType, materialId);
            materials.add(newMaterial);
            adapter.notifyItemInserted(materials.size() - 1);
            updatePieChart();
        }
    }

    private void updatePieChart() {
        if (pieChart == null) return;

        // 如果没有材料，显示100%未分配的饼图
        if (materials == null || materials.isEmpty()) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(100f, "未分配"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(Color.LTGRAY);
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(Color.WHITE);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());

            pieChart.setCenterText(mixRatioName);
            pieChart.setCenterTextSize(16f);
            pieChart.setCenterTextColor(Color.BLACK);
            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setData(data);
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        int[] colorArray = getChartColors();
        List<Integer> colors = new ArrayList<>();
        for (int color : colorArray) {
            colors.add(color);
        }

        float totalPercentage = 0;

        // 使用mixRatioName作为饼图标题
        pieChart.setCenterText(mixRatioName);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);

        // 添加已有材料
        for (MaterialItem material : materials) {
            if (material.getPercentage() > 0) {
                entries.add(new PieEntry(material.getPercentage(), material.getName()));
                totalPercentage += material.getPercentage();
            }
        }

        // 如果总百分比小于100，添加剩余部分
        if (totalPercentage < 100) {
            entries.add(new PieEntry(100 - totalPercentage, "未分配"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.rgb(64, 64, 64)); // 改为深灰色
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setValueLineColor(Color.rgb(64, 64, 64)); // 连接线也改为深灰色
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineWidth(2f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());

        pieChart.setEntryLabelColor(Color.rgb(64, 64, 64)); // 设置标签文字颜色为深灰色
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private int[] getChartColors() {
        return new int[]{
            Color.rgb(33, 150, 243),    // 明亮的蓝色
            Color.rgb(255, 152, 0),     // 明亮的橙色
            Color.rgb(76, 175, 80),     // 鲜艳的绿色
            Color.rgb(244, 67, 54),     // 鲜艳的红色
            Color.rgb(156, 39, 176),    // 深紫色
            Color.rgb(255, 193, 7),     // 明黄色
            Color.rgb(0, 150, 136),     // 青绿色
            Color.rgb(233, 30, 99),     // 粉红色
            Color.rgb(96, 125, 139),    // 蓝灰色
            Color.rgb(158, 158, 158)    // 用于未分配部分的灰色
        };
    }

    private void saveMixRatio() {
        Log.d(TAG, "开始保存配比数据");
        Log.d(TAG, "配比名称: " + mixRatioName);
        Log.d(TAG, "材料数量: " + materials.size());

        // 从 mixRatioName 字段获取配比名称
        if (mixRatioName == null || mixRatioName.isEmpty()) {
            showToast("请输入配比名称");
            return;
        }

        // 检查材料列表
        if (materials.isEmpty()) {
            showToast("请添加至少一种原料");
            return;
        }

        // 创建本地数据库的配比对象
        MixRatio mixRatio = new MixRatio();
        mixRatio.setName(mixRatioName);
        mixRatio.setCreationTime(System.currentTimeMillis());
        
        // 转换材料列表
        List<MaterialItem> materialsList = new ArrayList<>(materials);
        mixRatio.setMaterials(materialsList);

        // 打印每个材料的信息
        for (MaterialItem item : materialsList) {
            Log.d(TAG, String.format("材料: %s, 百分比: %.2f%%, 类型: %s",
                item.getName(), item.getPercentage(), item.getType()));
        }

        // 创建后端API请求对象
        MixRatioRequest request = new MixRatioRequest();
        request.setMixName(mixRatioName);
        
        // 获取用户信息并设置
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        long userId = sharedPrefsManager.getUserId();
        String companyStr = sharedPrefsManager.getUserCompany();
        
        // 设置用户ID和单位ID
        request.setCreatedBy(userId);
        
        // 尝试将公司字符串转换为Long ID
        try {
            if (companyStr != null && !companyStr.isEmpty()) {
                Long companyId = Long.parseLong(companyStr);
                request.setMixCompany(companyId);
                Log.d(TAG, "设置公司ID: " + companyId);
            } else {
                Log.w(TAG, "无法获取公司ID，使用默认值null");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "公司ID格式错误: " + companyStr, e);
        }
        
        Log.d(TAG, "设置用户ID: " + userId);
        
        List<MixRatioRequest.AsphaltComponentRequest> asphaltComponents = new ArrayList<>();
        List<MixRatioRequest.SandComponentRequest> sandComponents = new ArrayList<>();
        List<MixRatioRequest.StoneComponentRequest> stoneComponents = new ArrayList<>();
        
        for (MaterialItem item : materials) {
            Long materialId = extractMaterialIdFromMaterial(item);
            if (materialId == null) {
                Log.w(TAG, "材料ID为空: " + item.getName());
                continue;
            }
            
            if ("asphalt".equals(item.getType())) {
                MixRatioRequest.AsphaltComponentRequest component = new MixRatioRequest.AsphaltComponentRequest();
                component.setAsphaltId(materialId);
                component.setPercentage((double) item.getPercentage());
                asphaltComponents.add(component);
            } else if ("sand".equals(item.getType())) {
                MixRatioRequest.SandComponentRequest component = new MixRatioRequest.SandComponentRequest();
                component.setSandId(materialId);
                component.setGradation(extractGradationFromMaterial(item));
                component.setPercentage((double) item.getPercentage());
                sandComponents.add(component);
            } else if ("stone".equals(item.getType())) {
                MixRatioRequest.StoneComponentRequest component = new MixRatioRequest.StoneComponentRequest();
                component.setStoneId(materialId);
                component.setGradation(extractGradationFromMaterial(item));
                component.setPercentage((double) item.getPercentage());
                stoneComponents.add(component);
            }
        }
        
        request.setAsphaltComponents(asphaltComponents);
        request.setSandComponents(sandComponents);
        request.setStoneComponents(stoneComponents);

        // 显示加载对话框
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
            .setTitle("正在保存")
            .setMessage("请稍候...")
            .setCancelable(false)
            .create();
        loadingDialog.show();

        // 同时保存到本地数据库和后端
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        new Thread(() -> {
            try {
                Log.d(TAG, "正在执行数据库插入操作");
                long id = db.insertMixRatio(mixRatio);
                Log.d(TAG, "本地数据库保存成功，ID：" + id);
                
                // 后端API调用保存到服务器
                MixRatioApiService apiService = new MixRatioApiService(MixRatioEditActivity.this);
                Log.d(TAG, "调用createMixRatio API, 配比名称: " + request.getMixName());
                apiService.createMixRatio(request, new Callback<ApiResponse<MixRatioResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MixRatioResponse>> call, 
                                         Response<ApiResponse<MixRatioResponse>> response) {
                        // 关闭加载对话框
                        runOnUiThread(() -> {
                            if (loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                        });
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // 处理成功响应
                            MixRatioResponse mixRatioResponse = response.body().getData();
                            Log.d(TAG, "后端保存成功，ID: " + mixRatioResponse.getId() + 
                                  ", 配比编号: " + mixRatioResponse.getMixId());
                            
                            runOnUiThread(() -> {
                                showToast("配比保存成功");
                                setResult(RESULT_OK);
                                finish();
                            });
                        } else {
                            // 处理错误响应
                            String errorMsg = "保存失败";
                            if (response.body() != null) {
                                errorMsg = response.body().getMessage();
                            } else if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    Log.e(TAG, "解析错误响应失败", e);
                                }
                            }
                            
                            final String finalErrorMsg = errorMsg;
                            runOnUiThread(() -> {
                                showToast("后端保存失败: " + finalErrorMsg);
                                // 如果后端保存失败但本地保存成功，仍然算作部分成功
                                setResult(RESULT_OK);
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MixRatioResponse>> call, Throwable t) {
                        // 关闭加载对话框
                        runOnUiThread(() -> {
                            if (loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                        });
                        
                        // 处理网络或其他错误
                        Log.e(TAG, "后端API调用失败", t);
                        runOnUiThread(() -> {
                            showToast("网络错误: " + t.getMessage() + "，但数据已保存到本地");
                            // 即使网络错误，只要本地保存成功，仍然返回RESULT_OK
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "保存失败", e);
                runOnUiThread(() -> {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    showToast("保存失败: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * 从材料项中提取材料ID
     */
    private Long extractMaterialIdFromMaterial(MaterialItem item) {
        // 直接使用getMaterialId方法获取材料ID
        return item.getMaterialId();
    }
    
    /**
     * 从材料项中提取级配信息
     */
    private String extractGradationFromMaterial(MaterialItem item) {
        // 从原料显示名称中提取级配，假设格式为"...级配：XXX..."
        String name = item.getName();
        int startIndex = name.indexOf("级配：") + 3;
        if (startIndex < 3) return ""; // 没有找到级配信息
        
        int endIndex = name.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = name.length();
        
        return name.substring(startIndex, endIndex).trim();
    }
}
