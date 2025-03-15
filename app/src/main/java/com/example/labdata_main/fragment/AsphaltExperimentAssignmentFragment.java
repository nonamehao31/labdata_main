package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.R;
import com.example.labdata_main.api.request.AsphaltExperimentRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.AsphaltExperimentResponse;
import com.example.labdata_main.api.service.AsphaltExperimentService;
import com.example.labdata_main.utils.ApiClient;
import com.example.labdata_main.api.ApiConfig;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.dialog.ExperimentSelectionBottomSheetDialog;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AsphaltExperimentAssignmentFragment extends Fragment {
    private static final String ARG_TASK_NAME = "task_name";
    private static final String TAG = "AsphaltExperimentAssignmentFragment";
    
    private String taskName;
    private LinearLayout cardContainer;
    private AsphaltSelectionFragment asphaltSelectionFragment;
    private MaterialButton btnConfirm;
    private Map<AsphaltInfo, Set<ExperimentType>> asphaltExperiments = new HashMap<>();
    private Executor executor;
    private AppDatabase database;
    private Set<AsphaltInfo> selectedAsphalt;

    public static AsphaltExperimentAssignmentFragment newInstance(String taskName) {
        AsphaltExperimentAssignmentFragment fragment = new AsphaltExperimentAssignmentFragment();
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
        // 确保API客户端已初始化
        ApiClient.init(requireContext());
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };
        database = AppDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asphalt_experiment_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardContainer = view.findViewById(R.id.cardContainer);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        Log.e(TAG, "onViewCreated: 尝试获取AsphaltSelectionFragment");
        
        // 查找所有可能的fragment标签
        String[] possibleTags = {"f0", "f1", "android:switcher:viewPagerId:0", "AsphaltSelectionFragment"};
        for (String tag : possibleTags) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment instanceof AsphaltSelectionFragment) {
                asphaltSelectionFragment = (AsphaltSelectionFragment) fragment;
                Log.e(TAG, "找到AsphaltSelectionFragment，标签: " + tag);
                break;
            }
        }
        
        // 如果通过标签没找到，尝试通过类型搜索所有fragment
        if (asphaltSelectionFragment == null) {
            Log.e(TAG, "通过标签未找到AsphaltSelectionFragment，尝试其他方法");
            List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof AsphaltSelectionFragment) {
                    asphaltSelectionFragment = (AsphaltSelectionFragment) fragment;
                    Log.e(TAG, "在碎片列表中找到了AsphaltSelectionFragment");
                    break;
                }
            }
        }

        if (asphaltSelectionFragment != null) {
            Log.e(TAG, "获取到AsphaltSelectionFragment，尝试获取选中的沥青");
            selectedAsphalt = asphaltSelectionFragment.getSelectedAsphalt();
            Log.e(TAG, "选中的沥青数量: " + (selectedAsphalt != null ? selectedAsphalt.size() : 0));
            if (selectedAsphalt != null && !selectedAsphalt.isEmpty()) {
                createAsphaltCards(selectedAsphalt);
            } else {
                Log.e(TAG, "未获取到选中的沥青信息");
                Toast.makeText(requireContext(), "请先选择沥青材料", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "未找到AsphaltSelectionFragment");
            Toast.makeText(requireContext(), "无法获取沥青选择信息", Toast.LENGTH_SHORT).show();
        }

        // 确保按钮点击事件被正确设置，添加更多日志
        btnConfirm.setOnClickListener(v -> {
            Log.e(TAG, "确认按钮被点击，准备保存实验任务...");
            saveExperimentTask();
        });
    }

    private void createAsphaltCards(Set<AsphaltInfo> asphaltInfos) {
        Log.e(TAG, "创建沥青卡片，数量: " + asphaltInfos.size());
        cardContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (AsphaltInfo asphalt : asphaltInfos) {
            Log.e(TAG, "添加沥青卡片: " + asphalt.getGrade() + " " + asphalt.getType());
            MaterialCardView card = (MaterialCardView) inflater.inflate(
                    R.layout.card_asphalt_experiment_assignment, cardContainer, false);

            TextView tvGrade = card.findViewById(R.id.tvGrade);
            TextView tvType = card.findViewById(R.id.tvType);
            TextView tvSupplier = card.findViewById(R.id.tvSupplier);
            MaterialButton btnSelectExperiments = card.findViewById(R.id.btnSelectExperiments);
            TextView tvSelectedExperiments = card.findViewById(R.id.tvSelectedExperiments);

            tvGrade.setText(asphalt.getGrade());
            tvType.setText(asphalt.getType());
            tvSupplier.setText(asphalt.getSupplier());

            btnSelectExperiments.setOnClickListener(v -> showExperimentSelectionDialog(asphalt, tvSelectedExperiments));

            cardContainer.addView(card);
        }
    }

    private void showExperimentSelectionDialog(AsphaltInfo asphalt, TextView tvSelectedExperiments) {
        ExperimentSelectionBottomSheetDialog dialog = new ExperimentSelectionBottomSheetDialog();
        dialog.setOnExperimentsSelectedListener(selectedExperiments -> {
            // 更新显示
            updateSelectedExperimentsView(tvSelectedExperiments, selectedExperiments);
            
            // 更新数据库中的任务
            executor.execute(() -> {
                String taskName = getArguments().getString(ARG_TASK_NAME);
                ExperimentTask task = database.experimentTaskDao().getTaskByName(taskName);
                if (task != null) {
                    // 设置截止日期
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date expiryDate = dateFormat.parse(asphalt.getExpiryDate());
                        if (expiryDate != null) {
                            task.setDeadline(expiryDate.getTime());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 将选中的实验和沥青信息保存到notes字段
                    StringBuilder notes = new StringBuilder();
                    notes.append("沥青信息:\n");
                    notes.append("标号: ").append(asphalt.getGrade()).append("\n");
                    notes.append("类型: ").append(asphalt.getType()).append("\n");
                    notes.append("供应商: ").append(asphalt.getSupplier()).append("\n");
                    notes.append("检测截止日期: ").append(asphalt.getExpiryDate()).append("\n\n");
                    notes.append("选中的实验:\n");
                    int index = 1;
                    for (ExperimentType experiment : selectedExperiments) {
                        notes.append(index++).append(". ").append(experiment.getName()).append("\n");
                    }
                    task.setNotes(notes.toString());
                    
                    // 保存到数据库
                    database.experimentTaskDao().update(task);
                }
            });
            
            // 关键修改：将所选实验保存到Fragment的asphaltExperiments映射中
            Log.e(TAG, "为沥青保存实验选择: " + asphalt.getGrade() + ", 实验数量: " + selectedExperiments.size());
            asphaltExperiments.put(asphalt, selectedExperiments);
            
            // 显示Toast通知
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), 
                    "已为 " + asphalt.getGrade() + " 选择了 " + selectedExperiments.size() + " 个实验", 
                    Toast.LENGTH_SHORT).show();
            });
        });
        dialog.show(getChildFragmentManager(), "experiment_selection");
    }

    private void updateSelectedExperimentsView(TextView textView, Set<ExperimentType> experiments) {
        if (experiments.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (ExperimentType experiment : experiments) {
                sb.append(index++).append(". ").append(experiment.getName()).append("\n");
            }
            // 移除最后一个换行符
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            textView.setText(sb.toString());
        }
    }

    private void updateConfirmButtonState() {
        boolean allAsphaltHasExperiments = true;
        for (AsphaltInfo asphalt : asphaltSelectionFragment.getSelectedAsphalt()) {
            Set<ExperimentType> experiments = asphaltExperiments.get(asphalt);
            if (experiments == null || experiments.isEmpty()) {
                allAsphaltHasExperiments = false;
                break;
            }
        }
        btnConfirm.setEnabled(allAsphaltHasExperiments);
    }

    public void saveExperimentTask() {
        Log.e(TAG, "开始保存实验任务流程...");
        
        // 检查是否有选中的沥青实验
        Log.e(TAG, "当前asphaltExperiments大小: " + asphaltExperiments.size());
        boolean hasSelectedExperiments = false;
        for (Set<ExperimentType> experiments : asphaltExperiments.values()) {
            if (!experiments.isEmpty()) {
                hasSelectedExperiments = true;
                break;
            }
        }
        
        if (!hasSelectedExperiments) {
            Toast.makeText(requireContext(), "请为至少一种沥青选择实验", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "没有选择任何实验，无法保存");
            return;
        }
        
        // 获取公司ID
        SharedPrefsManager prefsManager = new SharedPrefsManager(requireContext());
        String companyId = prefsManager.getUserCompany();
        Log.e(TAG, "获取到的公司ID: " + companyId);
        
        // 生成任务ID
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String baseTaskId = "ASPH_" + dateFormat.format(new Date());
        Log.e(TAG, "生成的基础任务ID: " + baseTaskId);
        
        // 创建实验任务列表
        List<ExperimentTask> tasks = new ArrayList<>();
        
        // 创建要发送到后端的沥青实验请求列表
        List<AsphaltExperimentRequest> asphaltExperimentRequests = new ArrayList<>();
        
        // 打印构建请求前的信息
        Log.e(TAG, "当前沥青实验映射数据:");
        for (Map.Entry<AsphaltInfo, Set<ExperimentType>> entry : asphaltExperiments.entrySet()) {
            AsphaltInfo asphalt = entry.getKey();
            Set<ExperimentType> experiments = entry.getValue();
            Log.e(TAG, "沥青: " + asphalt.getGrade() + ", 实验数量: " + experiments.size());
        }
        
        // 创建实验任务
        int taskIndex = 0;
        for (Map.Entry<AsphaltInfo, Set<ExperimentType>> entry : asphaltExperiments.entrySet()) {
            AsphaltInfo asphalt = entry.getKey();
            Set<ExperimentType> experiments = entry.getValue();
            
            ExperimentTask task = new ExperimentTask();
            // 设置任务ID，格式：ASPH_yyyyMMdd_HHmmss_序号
            String fullTaskId = baseTaskId + "_" + taskIndex;
            task.setTaskId(fullTaskId);
            Log.e(TAG, "Setting task ID: " + fullTaskId);
            
            task.setTaskName(taskName);
            task.setExperimentType("ASPHALT");
            task.setStatus("未接受");
            task.setCreationTime(System.currentTimeMillis());
            task.setCompanyId(String.valueOf(companyId)); // 已修改为String类型
            
            // 解析检测截止日期字符串为时间戳
            try {
                SimpleDateFormat expiryDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date expiryDate = expiryDateFormat.parse(asphalt.getExpiryDate());
                if (expiryDate != null) {
                    task.setDeadline(expiryDate.getTime());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 设置实验分配
            Map<Long, List<String>> experimentAssignments = new HashMap<>();
            List<String> experimentTypes = new ArrayList<>();
            for (ExperimentType experiment : experiments) {
                experimentTypes.add(experiment.getType());
                
                // 为每个沥青实验创建一个请求对象
                AsphaltExperimentRequest experimentRequest = new AsphaltExperimentRequest(
                    experiment.getName(),  // 实验名称现在应该是任务分派内容
                    asphalt.getGrade() + "_" + taskName // 使用沥青标号+任务名作为任务名称
                );
                
                // 添加调试日志 - 检查沥青ID是否正确设置
                Long asphaltId = asphalt.getId();
                Log.e(TAG, "创建沥青实验请求，沥青ID: " + asphaltId + ", 沥青等级: " + asphalt.getGrade());
                
                // 设置选定的沥青ID
                experimentRequest.setSelectedAsphaltId(asphaltId);
                
                // 再次检查请求对象中的沥青ID
                Log.e(TAG, "请求对象中的沥青ID: " + experimentRequest.getSelectedAsphaltId());
                
                asphaltExperimentRequests.add(experimentRequest);
                
                Log.e(TAG, "添加沥青实验请求: 任务分派=" + experiment.getName() + ", 任务名称=" + 
                    (asphalt.getGrade() + "_" + taskName));
            }
            // 使用 0L 作为默认的配比ID，因为沥青实验不需要配比
            experimentAssignments.put(0L, experimentTypes);
            task.setExperimentAssignments(experimentAssignments);

            // 将沥青信息存储在notes字段中
            StringBuilder notes = new StringBuilder();
            notes.append("沥青信息\n");
            notes.append("标号: ").append(asphalt.getGrade()).append("\n");
            notes.append("类型: ").append(asphalt.getType()).append("\n");
            notes.append("供应商: ").append(asphalt.getSupplier()).append("\n");
            notes.append("---\n"); // 添加分隔符
            notes.append("实验指派\n");
            for (ExperimentType experiment : experiments) {
                notes.append(experiment.getName()).append("\n");
            }
            task.setNotes(notes.toString());
            tasks.add(task);
            taskIndex++;
        }

        // 显示加载提示
        Toast.makeText(requireContext(), "正在保存...", Toast.LENGTH_SHORT).show();
        
        // 直接测试网络连接可达性
        new Thread(() -> {
            try {
                String host = ApiConfig.BASE_URL.replace("http://", "").replace("https://", "").split("/")[0];
                String[] hostParts = host.split(":");
                String ip = hostParts[0];
                int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 80;
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "正在测试连接: " + ip + ":" + port, Toast.LENGTH_LONG).show();
                });
                
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(ip, port), 3000);
                boolean isConnected = socket.isConnected();
                socket.close();
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        "服务器连接测试: " + (isConnected ? "成功" : "失败"), 
                        Toast.LENGTH_LONG).show();
                });
                
                // 网络连接成功后，再继续进行API调用
                if (isConnected) {
                    requireActivity().runOnUiThread(() -> {
                        // 在UI线程执行API调用
                        executeApiCall(asphaltExperimentRequests, tasks);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        "连接测试失败: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void executeApiCall(List<AsphaltExperimentRequest> asphaltExperimentRequests, List<ExperimentTask> tasks) {
        // 创建AsphaltExperimentService实例，使用正确的API客户端实现
        AsphaltExperimentService asphaltExperimentService = ApiClient.getClient().create(AsphaltExperimentService.class);
        
        // 打印日志，显示要发送的请求
        Log.d(TAG, "准备发送沥青实验请求，数量: " + asphaltExperimentRequests.size());
        for (AsphaltExperimentRequest req : asphaltExperimentRequests) {
            Log.d(TAG, "沥青实验请求: 任务分派=" + req.getAsphaltTaskAssignment() + 
                  ", 任务名称=" + req.getAsphaltTaskName() + 
                  ", 沥青ID=" + req.getSelectedAsphaltId());
        }
        Log.e(TAG, "API基础URL: " + ApiConfig.BASE_URL);
        
        if (!asphaltExperimentRequests.isEmpty()) {
            Log.e(TAG, "开始发送批量创建沥青实验请求: " + asphaltExperimentRequests.size() + "条");
            
            // 使用Toast显示直观调试信息
            Toast.makeText(requireContext(), "正在发送API请求：" + asphaltExperimentRequests.size() + "条实验数据", Toast.LENGTH_LONG).show();
            
            try {
                asphaltExperimentService.createAsphaltExperiments(asphaltExperimentRequests).enqueue(new Callback<ApiResponse<List<AsphaltExperimentResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AsphaltExperimentResponse>>> call, Response<ApiResponse<List<AsphaltExperimentResponse>>> response) {
                        Log.e(TAG, "收到服务器响应: code=" + response.code());
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.e(TAG, "沥青实验保存成功，响应数据: " + response.body().getData().size());
                            
                            // 实验数据保存成功后，再保存本地任务
                            saveLocalTasks(tasks);
                        } else {
                            String errorMessage = "保存失败";
                            if (response.body() != null) {
                                errorMessage = response.body().getMessage();
                            } else if (response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string();
                                } catch (Exception e) {
                                    Log.e(TAG, "无法解析错误信息", e);
                                }
                            }
                            Log.e(TAG, "保存沥青实验失败: " + errorMessage);
                            Log.e(TAG, "HTTP错误代码: " + response.code());
                            Log.e(TAG, "请求URL: " + call.request().url());
                            Log.e(TAG, "请求方法: " + call.request().method());
                            
                            Toast.makeText(requireContext(), "保存沥青实验失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                            
                            // 即使API请求失败，也保存本地任务
                            saveLocalTasks(tasks);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<List<AsphaltExperimentResponse>>> call, Throwable t) {
                        Log.e(TAG, "API请求发送失败", t);
                        Log.e(TAG, "请求URL: " + call.request().url());
                        Log.e(TAG, "请求方法: " + call.request().method());
                        Log.e(TAG, "请求体: " + call.request().toString());
                        
                        Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        
                        // 即使网络请求失败，也保存本地任务
                        saveLocalTasks(tasks);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "发送API请求时发生异常", e);
                Toast.makeText(requireContext(), "处理请求时发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // 即使网络请求失败，也保存本地任务
                saveLocalTasks(tasks);
            }
        } else {
            Log.e(TAG, "没有沥青实验请求数据，只保存本地任务");
            saveLocalTasks(tasks);
        }
    }
    
    /**
     * 保存本地任务
     */
    private void saveLocalTasks(List<ExperimentTask> tasks) {
        // 在后台线程中保存任务
        executor.execute(() -> {
            try {
                for (ExperimentTask task : tasks) {
                    database.experimentTaskDao().insert(task);
                }
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "任务已创建", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "创建任务失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
