package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.adapter.DeviceAdapter;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 设备管理界面
 * 显示用户所属单位的全部设备，提供添加设备功能
 * 支持按设备类型筛选
 */
public class DeviceManagementActivity extends AppCompatActivity {

    private static final String TAG = "DeviceManagementActivity";
    private static final int REQUEST_ADD_DEVICE = 1001;
    
    // 设备类型常量
    private static final String TYPE_ALL = "all";
    private static final String TYPE_MIXING = "mixing";
    private static final String TYPE_FORMING = "forming";
    private static final String TYPE_TEST = "test";

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private LinearLayout emptyStateContainer;
    private FloatingActionButton fabAddDevice;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private ImageButton btnBack;
    private ChipGroup chipGroup;
    private Chip chipAll, chipMixing, chipForming, chipTest;
    
    private String companyId;
    private ApiService apiService;
    private List<Device> deviceList = new ArrayList<>();
    private List<Device> filteredDeviceList = new ArrayList<>();
    private String currentFilter = TYPE_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);

        // 获取公司ID
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        companyId = sharedPrefsManager.getUserCompany();
        
        // 如果从Intent获取到公司ID，则覆盖SharedPrefs中的值
        if (getIntent().hasExtra("company_id")) {
            companyId = getIntent().getStringExtra("company_id");
        }

        // 初始化API服务
        apiService = ApiClient.getClient().create(ApiService.class);

        // 初始化视图
        initViews();

        // 加载设备列表
        loadDevices();
    }

    private void initViews() {
        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 初始化基本控件
        recyclerView = findViewById(R.id.recyclerViewDevices);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        fabAddDevice = findViewById(R.id.fabAddDevice);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(this, filteredDeviceList);
        recyclerView.setAdapter(deviceAdapter);

        // 初始化Chip组件
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipMixing = findViewById(R.id.chipMixing);
        chipForming = findViewById(R.id.chipForming);
        chipTest = findViewById(R.id.chipTest);
        
        // 设置Chip组件点击事件
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // 如果没有选中任何项，默认选中“全部”
                chipAll.setChecked(true);
                return;
            }
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = TYPE_ALL;
            } else if (checkedId == R.id.chipMixing) {
                currentFilter = TYPE_MIXING;
            } else if (checkedId == R.id.chipForming) {
                currentFilter = TYPE_FORMING;
            } else if (checkedId == R.id.chipTest) {
                currentFilter = TYPE_TEST;
            }
            
            filterDevices();
        });

        // 设置添加设备按钮点击事件
        fabAddDevice.setOnClickListener(v -> {
            Intent intent = new Intent(this, EquipmentGuideActivity.class);
            intent.putExtra("company_id", companyId);
            startActivityForResult(intent, REQUEST_ADD_DEVICE);
        });

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadDevices);
        
        // 设置刷新颜色
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void loadDevices() {
        if (companyId == null || companyId.isEmpty()) {
            Log.e(TAG, "无法获取公司ID");
            Toast.makeText(this, "无法获取公司信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示加载进度
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);

        // 调用API获取设备列表
        Call<ApiResponse<List<DeviceResponse>>> call = apiService.getDevicesByCompany(Long.parseLong(companyId));
        
        call.enqueue(new Callback<ApiResponse<List<DeviceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DeviceResponse>>> call, Response<ApiResponse<List<DeviceResponse>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deviceList.clear();
                    List<DeviceResponse> deviceResponses = response.body().getData();
                    if (deviceResponses != null && !deviceResponses.isEmpty()) {
                        // 将DeviceResponse转换为Device对象
                        for (DeviceResponse resp : deviceResponses) {
                            Device device = new Device(
                                resp.getId(),
                                resp.getType(),
                                resp.getManufacturer(),
                                resp.getModel(),
                                resp.getPurchaseYear(),
                                resp.getCompanyId());
                            deviceList.add(device);
                        }
                        
                        // 应用当前筛选器并更新UI
                        filterDevices();
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState("暂无设备数据\n点击右下角按钮添加设备");
                    }
                } else {
                    String errorMsg = "获取设备列表失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(DeviceManagementActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyState("无法获取设备数据\n" + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DeviceResponse>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "网络请求失败：" + t.getMessage());
                Toast.makeText(DeviceManagementActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                showEmptyState("网络连接失败\n请检查网络后重试");
            }
        });
    }
    
    /**
     * 根据当前筛选器过滤设备列表
     */
    private void filterDevices() {
        filteredDeviceList.clear();
        
        if (currentFilter.equals(TYPE_ALL)) {
            // 全部设备
            filteredDeviceList.addAll(deviceList);
        } else {
            // 根据类型过滤
            for (Device device : deviceList) {
                String type = device.getType().toLowerCase();
                switch (currentFilter) {
                    case TYPE_MIXING:
                        // 拌合设备
                        if (type.contains("拌合") || type.contains("mixing") || 
                            type.contains("搅拌") || type.contains("mixer")) {
                            filteredDeviceList.add(device);
                        }
                        break;
                    case TYPE_FORMING:
                        // 压实设备
                        if (type.contains("压实") || type.contains("compaction") || 
                            type.contains("压路") || type.contains("压实机") ||
                            type.contains("forming") || type.equals("FORMING")) {
                            filteredDeviceList.add(device);
                        }
                        break;
                    case TYPE_TEST:
                        // 实验设备
                        if (type.contains("实验") || type.contains("test") || 
                            type.contains("检测") || type.contains("apparatus")) {
                            filteredDeviceList.add(device);
                        }
                        break;
                }
            }
        }
        
        deviceAdapter.notifyDataSetChanged();
        
        // 检查筛选后是否有数据
        if (filteredDeviceList.isEmpty()) {
            if (deviceList.isEmpty()) {
                showEmptyState("暂无设备数据\n点击右下角按钮添加设备");
            } else {
                showEmptyState("没有找到对应类型的设备\n请尝试其他类型或点击右下角添加设备");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
        
        // 根据设备类型设置设备类型指示条颜色
        for (int i = 0; i < filteredDeviceList.size(); i++) {
            Device device = filteredDeviceList.get(i);
            String type = device.getType().toLowerCase();
            int color;
            
            if (type.contains("拌合") || type.contains("mixing") || 
                type.contains("搅拌") || type.contains("mixer")) {
                color = 0xFF4CAF50; // 绿色
            } else if (type.contains("压实") || type.contains("compaction") || 
                       type.contains("压路") || type.contains("压实机") ||
                       type.contains("forming") || type.equals("FORMING")) {
                color = 0xFF2196F3; // 蓝色
            } else if (type.contains("实验") || type.contains("test") || 
                       type.contains("检测") || type.contains("apparatus")) {
                color = 0xFFFF9800; // 橙色
            } else {
                color = 0xFF9E9E9E; // 灰色
            }
            
            device.setIndicatorColor(color);
        }
    }
    
    /**
     * 显示空状态提示
     */
    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        tvEmptyView.setText(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_DEVICE && resultCode == RESULT_OK) {
            // 添加设备成功，显示提示并刷新列表
            Toast.makeText(this, "设备添加成功", Toast.LENGTH_SHORT).show();
            
            // 在加载之前显示进度条
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
            
            // 延迟短暂后再加载数据，确保用户能看到添加成功提示
            recyclerView.postDelayed(this::loadDevices, 300);
        }
    }
}
