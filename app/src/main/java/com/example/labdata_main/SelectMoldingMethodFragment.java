package com.example.labdata_main;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.adapter.MoldingMethodAdapter;
import com.example.labdata_main.api.CompactionMethodApiService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.fragment.CompactionMethodFragment;
import com.example.labdata_main.fragment.MixingMethodFragment;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectMoldingMethodFragment extends Fragment implements MixingMethodFragment.OnNextStepListener {

    private static final String ARG_MIX_RATIOS = "selectedMixRatios";

    public static SelectMoldingMethodFragment newInstance(ArrayList<? extends Parcelable> mixRatios) {
        SelectMoldingMethodFragment fragment = new SelectMoldingMethodFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_MIX_RATIOS, mixRatios);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView rvMoldingMethods;
    private TextView emptyView;
    private MaterialCardView cardAddMoldingMethod;
    private MoldingMethodAdapter moldingMethodAdapter;
    private List<MoldingMethod> moldingMethods = new ArrayList<>();
    private ViewPager2 viewPager;
    private List<MoldingMethod> selectedMethods = new ArrayList<>();
    private AppDatabase database;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();
    private MoldingMethod selectedMoldingMethod;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(requireContext());
        
        // 从 Bundle 中获取选中的配比列表
        if (getArguments() != null && getArguments().containsKey(ARG_MIX_RATIOS)) {
            ArrayList<Parcelable> mixRatios = getArguments().getParcelableArrayList(ARG_MIX_RATIOS);
            Log.d("SelectMoldingMethodFragment", "Received mix ratios from bundle: " + (mixRatios != null ? mixRatios.size() : 0));
            if (mixRatios != null) {
                for (Parcelable p : mixRatios) {
                    if (p instanceof MixRatio) {
                        selectedMixRatios.add((MixRatio) p);
                    }
                }
                Log.d("SelectMoldingMethodFragment", "Added mix ratios to list: " + selectedMixRatios.size());
            }
        } else {
            Log.d("SelectMoldingMethodFragment", "No mix ratios in bundle");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_molding_method, container, false);

        initViews(view);
        setupListeners();
        setupRecyclerView();
        
        // 从API加载制件方法
        loadMoldingMethods();

        return view;
    }

    private void initViews(View view) {
        rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        emptyView = view.findViewById(R.id.emptyView_at_molding);
        cardAddMoldingMethod = view.findViewById(R.id.cardAddMoldingMethod);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupListeners() {
        cardAddMoldingMethod.setOnClickListener(v -> showMoldingMethodBottomSheet());
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadMoldingMethods);
        }
    }

    private void setupRecyclerView() {
        moldingMethodAdapter = new MoldingMethodAdapter(
            moldingMethods, 
            this::onDeleteMoldingMethod,
            this::onMoldingMethodSelected
        );
        
        // 设置可用的配比列表
        if (selectedMixRatios != null && !selectedMixRatios.isEmpty()) {
            moldingMethodAdapter.setAvailableMixRatios(selectedMixRatios);
        }
        
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMoldingMethods.setAdapter(moldingMethodAdapter);
        updateEmptyView();
    }

    private void loadMoldingMethods() {
        Log.d("SelectMoldingMethod", "开始加载制件方法数据...");
        
        // 如果适当，显示刷新动画
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        try {
            // 初始化数据库
            if (database == null) {
                database = AppDatabase.getInstance(requireContext());
            }
            
            // 获取API服务
            CompactionMethodApiService apiService = null;
            try {
                apiService = LabDataApplication.getCompactionMethodApiService();
                if (apiService == null) {
                    // 如果从Application中获取失败，尝试创建新实例
                    apiService = new CompactionMethodApiService(requireContext());
                    Log.d("SelectMoldingMethod", "从Application获取API服务失败，已创建新实例");
                }
            } catch (Exception e) {
                Log.e("SelectMoldingMethod", "尝试获取API服务时发生异常: " + e.getMessage());
                apiService = new CompactionMethodApiService(requireContext());
                Log.d("SelectMoldingMethod", "已创建新的API服务实例");
            }
            
            if (apiService == null) {
                Log.e("SelectMoldingMethod", "无法创建API服务，回退到本地数据库");
                showErrorAndFallbackToLocal("无法创建API服务");
                return;
            }
            
            Log.d("SelectMoldingMethod", "准备调用API服务");
            
            // 调用API获取制件方法
            apiService.getOrganizationCompactionMethods(new Callback<ApiResponse<List<MoldingMethod>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<MoldingMethod>>> call, Response<ApiResponse<List<MoldingMethod>>> response) {
                    if (getActivity() == null) {
                        Log.d("SelectMoldingMethod", "Fragment已分离，忽略API响应");
                        return;
                    }
                    
                    Log.d("SelectMoldingMethod", "收到API响应: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<MoldingMethod>> apiResponse = response.body();
                        Log.d("SelectMoldingMethod", "API响应成功: " + (apiResponse.isSuccess() ? "成功" : "失败") + 
                              ", 消息: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            requireActivity().runOnUiThread(() -> {
                                // 更新UI
                                moldingMethods.clear();
                                List<MoldingMethod> methods = apiResponse.getData();
                                
                                // 验证每个制件方法的ID
                                for (MoldingMethod method : methods) {
                                    Log.d("SelectMoldingMethod", "加载制件方法: ID=" + method.getId() + 
                                          ", 压实方法=" + method.getCompactionMethod() +
                                          ", 拌合温度=" + method.getMixingTemperature() +
                                          ", 拌合速度=" + method.getMixingSpeed());
                                    
                                    // 检查ID是否有效 (避免使用无效ID如0或负数)
                                    if (method.getId() <= 0) {
                                        Log.w("SelectMoldingMethod", "检测到无效的制件方法ID: " + method.getId());
                                        continue;
                                    }
                                }
                                
                                moldingMethods.addAll(methods);
                                moldingMethodAdapter.notifyDataSetChanged();
                                
                                // 隐藏刷新动画
                                if (swipeRefreshLayout != null) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                                
                                updateEmptyView();
                                
                                // 保存到本地数据库以备离线使用
                                saveMoldingMethodsToLocalDb(methods);
                                
                                Log.d("SelectMoldingMethod", "成功从服务器获取" + moldingMethods.size() + "个制件方法");
                            });
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "未知错误";
                            Log.e("SelectMoldingMethod", "API返回错误: " + errorMsg);
                            showErrorAndFallbackToLocal("获取制件方法失败: " + errorMsg);
                        }
                    } else {
                        String errorMsg = "服务器响应错误，状态码: " + response.code();
                        Log.e("SelectMoldingMethod", errorMsg);
                        showErrorAndFallbackToLocal(errorMsg);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<List<MoldingMethod>>> call, Throwable t) {
                    if (getActivity() == null) return;  // 避免Fragment已分离的情况
                    
                    // 记录错误详情
                    Log.e("SelectMoldingMethod", "API调用失败", t);
                    showErrorAndFallbackToLocal("网络错误: " + t.getMessage());
                }
            });
            
            Log.d("SelectMoldingMethod", "API请求已发送，等待响应...");
        } catch (Exception e) {
            Log.e("SelectMoldingMethod", "加载制件方法时发生异常", e);
            showErrorAndFallbackToLocal("初始化API服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示错误并回退到本地数据库加载
     * @param errorMessage 错误信息
     */
    private void showErrorAndFallbackToLocal(String errorMessage) {
        if (getActivity() == null) return;  // 避免Fragment已分离的情况
        
        requireActivity().runOnUiThread(() -> {
            // 隐藏刷新动画
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            
            // 显示错误提示
            Toast.makeText(requireContext(), errorMessage + "，使用本地数据", Toast.LENGTH_SHORT).show();
            
            // 回退到本地数据库
            loadMoldingMethodsFromLocalDb();
        });
    }
    
    /**
     * 从本地数据库加载制件方法（作为备选方案）
     */
    private void loadMoldingMethodsFromLocalDb() {
        new Thread(() -> {
            try {
                List<MoldingMethod> methods = database.moldingMethodDao().getAllMoldingMethods();
                
                if (getActivity() == null) return;  // 避免Fragment已分离的情况
                
                requireActivity().runOnUiThread(() -> {
                    moldingMethods.clear();
                    moldingMethods.addAll(methods);
                    moldingMethodAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    
                    Log.d("SelectMoldingMethod", "已从本地数据库加载" + methods.size() + "个制件方法");
                });
            } catch (Exception e) {
                if (getActivity() == null) return;
                
                requireActivity().runOnUiThread(() -> {
                    Log.e("LoadMoldingMethods", "加载本地制件方法失败", e);
                    Toast.makeText(requireContext(), "加载制件方法失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void onMoldingMethodSelected(List<MoldingMethod> methods) {
        selectedMethods.clear();
        selectedMethods.addAll(methods);
        
        // 更新选中的单个制件方法（用于获取ID）
        if (!methods.isEmpty()) {
            selectedMoldingMethod = methods.get(0);
        } else {
            selectedMoldingMethod = null;
        }
        
        checkInputValidity();
    }

    private void onMoldingMethodSelectionChanged(MoldingMethod method, boolean isSelected) {
        if (isSelected) {
            selectedMethods.add(method);
        } else {
            selectedMethods.remove(method);
        }
        checkInputValidity();
    }

    private void showMoldingMethodBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_molding_method, null);
        
        TabLayout tabLayout = bottomSheetView.findViewById(R.id.tabLayoutMoldingMethod);
        viewPager = bottomSheetView.findViewById(R.id.viewPagerMoldingMethod);
        
        // 创建 ViewPager 适配器
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        
        // 连接 TabLayout 和 ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "拌合方式" : "压实方法");
        }).attach();
        
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // ViewPager 适配器
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? 
                new MixingMethodFragment(this::onNextStep) : 
                new CompactionMethodFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        private void onNextStep() {
            if (viewPager != null) {
                viewPager.setCurrentItem(1);
            }
        }
    }

    private void onDeleteMoldingMethod(MoldingMethod method) {
        new Thread(() -> {
            try {
                // 从数据库中删除
                database.moldingMethodDao().delete(method);

                // 在主线程更新 UI
                requireActivity().runOnUiThread(() -> {
                    moldingMethods.remove(method);
                    moldingMethodAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    Toast.makeText(requireContext(), "制件方法删除成功", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                // 在主线程显示错误信息
                requireActivity().runOnUiThread(() -> {
                    android.util.Log.e("DeleteMoldingMethod", "删除制件方法失败", e);
                    Toast.makeText(requireContext(), "删除制件方法失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    public void addMoldingMethod(MoldingMethod method) {
        new Thread(() -> {
            try {
                // 保存到数据库
                long id = database.moldingMethodDao().insert(method);
                method.setId(id);

                // 在主线程更新 UI
                requireActivity().runOnUiThread(() -> {
                    moldingMethods.add(method);
                    moldingMethodAdapter.notifyItemInserted(moldingMethods.size() - 1);
                    updateEmptyView();
                    Toast.makeText(requireContext(), "制件方法添加成功", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    android.util.Log.e("AddMoldingMethod", "添加制件方法失败", e);
                    Toast.makeText(requireContext(), "添加制件方法失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    public void updateMixRatios(ArrayList<MixRatio> mixRatios) {
        selectedMixRatios.clear();
        selectedMixRatios.addAll(mixRatios);
        if (moldingMethodAdapter != null) {
            moldingMethodAdapter.setAvailableMixRatios(selectedMixRatios);
        }
    }

    private void updateEmptyView() {
        emptyView.setVisibility(moldingMethods.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // 在Fragment恢复时刷新数据
        if (moldingMethods.isEmpty()) {
            loadMoldingMethods();
        }
        
        // 在Fragment恢复时更新按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).updateNextButton();
        }
    }

    private void checkInputValidity() {
        // 通知 Activity 更新按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).updateNextButton();
        }
    }

    @Override
    public void onNextStep() {
        // 这是一个空实现，因为实际的页面切换逻辑已经在 ViewPagerAdapter 中处理
    }

    public String getSelectedMoldingMethod() {
        return selectedMoldingMethod != null ? selectedMoldingMethod.getName() : "";
    }

    /**
     * 获取选中的制件方法ID列表
     * @return 制件方法ID列表
     */
    public List<Long> getSelectedMethodIds() {
        List<Long> methodIds = new ArrayList<>();
        // 如果存在选中的制件方法，则添加其ID
        if (selectedMoldingMethod != null) {
            methodIds.add(selectedMoldingMethod.getId());
        }
        return methodIds;
    }

    // 添加获取已选择制件方法列表的方法
    public List<MoldingMethod> getSelectedMethods() {
        return selectedMethods;
    }
    
    /**
     * 获取选中的配比列表，供子Fragment使用
     * @return 已选择的配比列表
     */
    public List<MixRatio> getSelectedMixRatios() {
        return selectedMixRatios;
    }

    /**
     * 将制件方法保存到本地数据库
     * @param methods 制件方法列表
     */
    private void saveMoldingMethodsToLocalDb(List<MoldingMethod> methods) {
        new Thread(() -> {
            try {
                // 清除旧数据
                database.moldingMethodDao().deleteAllMoldingMethods();
                
                // 保存新数据
                database.moldingMethodDao().insertAll(methods.toArray(new MoldingMethod[0]));
                
                Log.d("SelectMoldingMethod", "成功将" + methods.size() + "个制件方法保存到本地数据库");
            } catch (Exception e) {
                Log.e("SelectMoldingMethod", "保存制件方法到本地数据库失败", e);
            }
        }).start();
    }
}
