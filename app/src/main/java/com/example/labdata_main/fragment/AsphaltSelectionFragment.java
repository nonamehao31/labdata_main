package com.example.labdata_main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.R;
import com.example.labdata_main.adapter.AsphaltAdapter;
import com.example.labdata_main.api.request.TestAsphaltMaterialRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.TestAsphaltMaterialResponse;
import com.example.labdata_main.api.service.TestAsphaltMaterialService;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiConfig;
import com.example.labdata_main.dialog.AddAsphaltBottomSheetDialog;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsphaltSelectionFragment extends Fragment implements AddAsphaltBottomSheetDialog.OnAsphaltAddedListener {
    private static final String ARG_TASK_NAME = "task_name";
    private static final String TAG = "AsphaltSelectionFragment";
    
    private String taskName;
    private RecyclerView rvAsphalt;
    private AsphaltAdapter asphaltAdapter;
    private MaterialCardView addAsphaltCard;
    private MaterialButton btnNext;
    private ViewPager2 viewPager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<AsphaltInfo> asphaltList = new ArrayList<>();
    private TestAsphaltMaterialService asphaltService;
    private SharedPrefsManager sharedPrefsManager;

    public static AsphaltSelectionFragment newInstance(String taskName) {
        AsphaltSelectionFragment fragment = new AsphaltSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskName = getArguments().getString(ARG_TASK_NAME);
        }
        // 初始化SharedPrefsManager以便获取认证令牌
        sharedPrefsManager = new SharedPrefsManager(requireContext());
        
        // 在创建API服务之前，先检查认证状态并记录
        String authHeader = sharedPrefsManager.getAuthHeader();
        Log.d(TAG, "初始化API服务，认证头: " + (authHeader != null ? "已存在" : "不存在"));
        if (authHeader == null) {
            Log.w(TAG, "警告: 未找到认证令牌，用户可能需要重新登录");
        }
        
        // 确保ApiClient已初始化
        ApiClient.init(requireContext());
        
        // 创建API服务
        asphaltService = ApiClient.getClient().create(TestAsphaltMaterialService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asphalt_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = requireActivity().findViewById(R.id.viewPager);
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupAddAsphaltCard();
        setupNextButton();
        loadAsphaltMaterials();
    }

    private void initializeViews(View view) {
        rvAsphalt = view.findViewById(R.id.rvAsphalt);
        addAsphaltCard = view.findViewById(R.id.addAsphaltCard);
        btnNext = view.findViewById(R.id.btnNext);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.blue_light_custom,
                R.color.white,
                R.color.white);
        swipeRefreshLayout.setOnRefreshListener(this::loadAsphaltMaterials);
    }

    private void setupRecyclerView() {
        asphaltAdapter = new AsphaltAdapter(asphalt -> {
            // 处理点击事件，切换选中状态
            asphaltAdapter.toggleSelection(asphalt);
            updateNextButtonState();
        });
        
        rvAsphalt.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAsphalt.setAdapter(asphaltAdapter);
    }

    private void setupAddAsphaltCard() {
        addAsphaltCard.setOnClickListener(v -> showAddAsphaltDialog());
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            Set<AsphaltInfo> selectedAsphalt = asphaltAdapter.getSelectedAsphalt();
            if (selectedAsphalt.isEmpty()) {
                Toast.makeText(requireContext(), "请至少选择一种沥青", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到下一页
            viewPager.setCurrentItem(1);
        });
        updateNextButtonState();
    }

    private void updateNextButtonState() {
        Set<AsphaltInfo> selectedAsphalt = asphaltAdapter.getSelectedAsphalt();
        btnNext.setEnabled(!selectedAsphalt.isEmpty());
    }

    private void showAddAsphaltDialog() {
        AddAsphaltBottomSheetDialog dialog = new AddAsphaltBottomSheetDialog();
        dialog.setOnAsphaltAddedListener(this);
        dialog.show(getChildFragmentManager(), "AddAsphaltDialog");
    }

    @Override
    public void onAsphaltAdded(AsphaltInfo asphaltInfo) {
        // 在对话框中已经保存到后端并返回带ID的完整对象
        if (asphaltInfo.getId() != null) {
            asphaltList.add(asphaltInfo);
            updateAsphaltList();
        }
    }
    
    private void loadAsphaltMaterials() {
        swipeRefreshLayout.setRefreshing(true);
        
        // 再次检查认证状态，确保在发起请求前认证令牌有效
        String authHeader = sharedPrefsManager.getAuthHeader();
        Log.d(TAG, "发起API请求，认证头: " + (authHeader != null ? "已存在" : "不存在"));
        
        asphaltService.getActiveAsphaltMaterials().enqueue(new Callback<ApiResponse<List<TestAsphaltMaterialResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<TestAsphaltMaterialResponse>>> call, Response<ApiResponse<List<TestAsphaltMaterialResponse>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<TestAsphaltMaterialResponse> materials = response.body().getData();
                    Log.d(TAG, "成功获取到" + materials.size() + "条沥青材料数据");
                    
                    // 将后端数据转换为本地模型
                    asphaltList.clear();
                    for (TestAsphaltMaterialResponse material : materials) {
                        AsphaltInfo asphaltInfo = new AsphaltInfo(
                                material.getId(),
                                material.getAsphaltGrade(),
                                material.getAsphaltCatalog(),
                                material.getAsphaltSupplier(),
                                material.getAsphaltTestDue()
                        );
                        asphaltList.add(asphaltInfo);
                    }
                    
                    updateAsphaltList();
                } else {
                    String errorCode = "";
                    String errorMessage = "未知错误";
                    
                    if (response.code() == 401) {
                        errorCode = "401 Unauthorized";
                        errorMessage = "身份验证失败，请重新登录";
                        // 可能需要跳转到登录页面
                        navigateToLogin();
                    } else if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    
                    Log.e(TAG, "获取沥青材料失败: " + errorCode + " " + errorMessage);
                    Toast.makeText(requireContext(), "加载失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<TestAsphaltMaterialResponse>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "获取沥青材料请求失败", t);
                Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateAsphaltList() {
        asphaltAdapter.updateData(asphaltList);
        updateNextButtonState();
    }
    
    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        // 清除认证信息
        if (sharedPrefsManager != null) {
            sharedPrefsManager.clearAuthToken();
        }
        
        // 跳转到登录页面
        Intent intent = new Intent(requireContext(), com.example.labdata_main.LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    
    /**
     * 获取用户选择的沥青材料
     * @return 选中的沥青材料集合
     */
    public Set<AsphaltInfo> getSelectedAsphalt() {
        if (asphaltAdapter == null) {
            return java.util.Collections.emptySet();
        }
        return asphaltAdapter.getSelectedAsphalt();
    }
}
