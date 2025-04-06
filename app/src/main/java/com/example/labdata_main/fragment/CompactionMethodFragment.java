package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.R;
import com.example.labdata_main.SelectMoldingMethodFragment;
import com.example.labdata_main.adapter.MoldingMethodAdapter;
import com.example.labdata_main.api.SpecimenApiService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.Specimen;
import com.example.labdata_main.model.SpecimenCreateDTO;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompactionMethodFragment extends Fragment {
    private AutoCompleteTextView actvCompactionMethod;
    private ViewPager2 viewPager;
    private AppDatabase database;
    private SpecimenApiService specimenApiService;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compaction_method, container, false);

        actvCompactionMethod = view.findViewById(R.id.actvCompactionMethod);
        viewPager = requireActivity().findViewById(R.id.viewPagerMoldingMethod);
        database = AppDatabase.getInstance(requireContext());
        specimenApiService = new SpecimenApiService(requireContext());

        // 获取父Fragment中的mixRatios
        SelectMoldingMethodFragment parentFragment = (SelectMoldingMethodFragment) requireParentFragment();
        if (parentFragment != null) {
            selectedMixRatios = parentFragment.getSelectedMixRatios();
        }

        // 设置压实方法的选项
        String[] compactionMethods = {"马歇尔击实", "旋转压实", "轮碾压实", "震动压实"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            compactionMethods
        );
        actvCompactionMethod.setAdapter(adapter);

        view.findViewById(R.id.btnCompleteMoldingMethod).setOnClickListener(v -> {
            if (validateInput()) {
                createMoldingMethod();
            }
        });

        return view;
    }

    private boolean validateInput() {
        String compactionMethod = actvCompactionMethod.getText().toString().trim();

        if (compactionMethod.isEmpty()) {
            actvCompactionMethod.setError("请选择压实方法");
            return false;
        }

        return true;
    }

    private void createMoldingMethod() {
        SelectMoldingMethodFragment parentFragment = (SelectMoldingMethodFragment) 
            requireParentFragment();

        // 获取 ViewPager 和 MixingMethodFragment
        MixingMethodFragment mixingMethodFragment = (MixingMethodFragment) 
            getParentFragmentManager().findFragmentByTag("f0");

        if (mixingMethodFragment != null) {
            try {
                float mixingTemperature = mixingMethodFragment.getMixingTemperature();
                float mixingSpeed = mixingMethodFragment.getMixingSpeed();
                float mixingTime = mixingMethodFragment.getMixingTime();
                String compactionMethod = actvCompactionMethod.getText().toString().trim();

                // 创建MoldingMethod对象
                MoldingMethod moldingMethod = new MoldingMethod(
                    mixingTemperature, 
                    mixingSpeed, 
                    mixingTime, 
                    compactionMethod
                );

                // 添加到父Fragment，由父Fragment处理数据库操作
                parentFragment.addMoldingMethod(moldingMethod);

                // 同时保存到specimens表并发送到后端
                saveToSpecimensTable(moldingMethod);

                // 关闭底部弹窗
                View bottomSheet = requireView().getRootView();
                if (bottomSheet.getParent() instanceof View) {
                    View parent = (View) bottomSheet.getParent();
                    if (parent.getParent() instanceof BottomSheetDialog) {
                        ((BottomSheetDialog) parent.getParent()).dismiss();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效的数值", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "创建制件方法失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "无法获取拌合方法信息", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToSpecimensTable(MoldingMethod moldingMethod) {
        // 不再需要验证配比，直接保存制件方法相关信息
        new Thread(() -> {
            try {
                // 获取当前登录用户的ID
                SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(requireContext());
                long userId = sharedPrefsManager.getUserId();
                
                // 获取用户所属单位ID
                String userCompany = sharedPrefsManager.getUserCompany();
                long companyId = -1;
                try {
                    companyId = Long.parseLong(userCompany);
                } catch (NumberFormatException e) {
                    android.util.Log.e("CompactionMethodFragment", "无法解析单位ID: " + userCompany, e);
                }
                
                // 创建一个新的specimen记录
                Specimen specimen = new Specimen();
                specimen.setMixRatioId(null); // 设置为null，后端已移除@NotNull注解
                specimen.setMixingTemperature(moldingMethod.getMixingTemperature());
                specimen.setMixingSpeed(moldingMethod.getMixingSpeed());
                
                // 确保拌合时间不为null，并转换为Integer类型
                // 如果无法获取有效的拌合时间，则设置默认值为0
                try {
                    float mixingTimeFloat = moldingMethod.getMixingTime();
                    specimen.setMixingTime((int)mixingTimeFloat);
                } catch (Exception e) {
                    android.util.Log.e("CompactionMethodFragment", "拌合时间转换失败，设置默认值", e);
                    specimen.setMixingTime(0); // 设置默认值
                }
                
                specimen.setCompactionMethod(moldingMethod.getCompactionMethod());
                specimen.setCreationTime(System.currentTimeMillis());
                specimen.setCreatedBy(userId); // 设置创建者ID为当前登录用户ID
                specimen.setSpecimenCompany(companyId); // 设置所属单位ID
                
                // 默认值设置
                specimen.setCutShape("rectangle"); // 默认矩形，可根据需要修改
                specimen.setCutCount(1);
                
                // 保存到本地数据库
                long specimenId = database.specimenDao().insert(specimen);
                specimen.setId(specimenId);
                
                // 打印日志，便于调试
                android.util.Log.d("CompactionMethodFragment", "发送试件数据到后端 - 用户ID: " + specimen.getCreatedBy());
                
                // 使用新的DTO方法发送到后端，避免ID字段被发送
                requireActivity().runOnUiThread(() -> {
                    // 注意这里使用specimen对象而不是dto对象
                    specimenApiService.createSpecimenWithDTO(specimen, new Callback<ApiResponse<Specimen>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Specimen>> call, Response<ApiResponse<Specimen>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(requireContext(), "试件保存成功", Toast.LENGTH_SHORT).show();
                                
                                // 如果需要，可以处理后端返回的数据
                                Specimen savedSpecimen = response.body().getData();
                                if (savedSpecimen != null) {
                                    android.util.Log.d("CompactionMethodFragment", "后端返回的试件ID: " + savedSpecimen.getId());
                                    
                                    // 直接更新MoldingMethod对象的ID
                                    moldingMethod.setId(savedSpecimen.getId());
                                    
                                    // 通知UI线程更新适配器中的ID
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            try {
                                                // 查找并更新SelectMoldingMethodFragment中的moldingMethods集合
                                                Fragment parentFragment = getParentFragment();
                                                if (parentFragment != null && parentFragment.getChildFragmentManager() != null) {
                                                    for (Fragment fragment : parentFragment.getChildFragmentManager().getFragments()) {
                                                        if (fragment instanceof com.example.labdata_main.SelectMoldingMethodFragment) {
                                                            com.example.labdata_main.SelectMoldingMethodFragment selectFragment = 
                                                                (com.example.labdata_main.SelectMoldingMethodFragment) fragment;
                                                            
                                                            // 直接更新SelectMoldingMethodFragment中的所有MoldingMethod对象
                                                            boolean updated = selectFragment.updateMoldingMethodId(
                                                                specimen.getCompactionMethod(), savedSpecimen.getId());
                                                            
                                                            if (updated) {
                                                                android.util.Log.d("CompactionMethodFragment", 
                                                                    "成功更新SelectMoldingMethodFragment中的制件方法ID从 1 到 " + 
                                                                    savedSpecimen.getId());
                                                            } else {
                                                                android.util.Log.w("CompactionMethodFragment", 
                                                                    "未能在SelectMoldingMethodFragment中找到匹配的制件方法");
                                                            }
                                                            
                                                            break;
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                android.util.Log.e("CompactionMethodFragment", 
                                                    "刷新MoldingMethodAdapter失败", e);
                                            }
                                        });
                                    }
                                }
                            } else {
                                String errorMsg = "试件保存失败";
                                if (response.body() != null) {
                                    errorMsg += ": " + response.body().getMessage();
                                } else if (response.errorBody() != null) {
                                    try {
                                        errorMsg += ": " + response.errorBody().string();
                                    } catch (IOException e) {
                                        errorMsg += ": " + response.code();
                                    }
                                }
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                                android.util.Log.e("CompactionMethodFragment", "API错误: " + errorMsg);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Specimen>> call, Throwable t) {
                            Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            android.util.Log.e("CompactionMethodFragment", "网络错误", t);
                        }
                    });
                });
            } catch (Exception e) {
                android.util.Log.e("CompactionMethodFragment", "保存试件时出错", e);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "保存试件出错: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    public String getCompactionMethod() {
        return actvCompactionMethod.getText().toString().trim();
    }
}
