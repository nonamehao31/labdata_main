package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.adapter.MixRatioAdapter;
import com.example.labdata_main.adapter.MixRatioAdapter.OnMixRatioDeleteListener;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectMixRatioFragment extends Fragment implements MixRatioAdapter.OnMixRatioSelectedListener, OnMixRatioDeleteListener {
    private RecyclerView rvMixRatios;
    private MaterialCardView cardAddMixRatio;
    private TextView emptyView;
    private MixRatioAdapter mixRatioAdapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_mix_ratio, container, false);

        initViews(view);
        setupListeners();
        loadMixRatios();

        return view;
    }

    private void initViews(View view) {
        cardAddMixRatio = view.findViewById(R.id.cardAddMixRatio);
        rvMixRatios = view.findViewById(R.id.rvMixRatios);
        emptyView = view.findViewById(R.id.emptyView);

        // 初始化配比列表
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        mixRatioAdapter = new MixRatioAdapter(this, this);
        rvMixRatios.setAdapter(mixRatioAdapter);
    }

    private void setupListeners() {
        cardAddMixRatio.setOnClickListener(v -> {
            // 跳转到添加配比界面
            Intent intent = new Intent(requireContext(), MixRatioEditActivity.class);
            startActivity(intent);
        });
    }

    private void loadMixRatios() {
        executorService.execute(() -> {
            List<MixRatio> mixRatios = databaseHelper.mixRatioDao().getAllMixRatios();
            requireActivity().runOnUiThread(() -> {
                mixRatioAdapter.submitList(mixRatios);
                updateEmptyView(mixRatios.isEmpty());
                checkInputValidity();
            });
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMixRatios.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void checkInputValidity() {
        boolean isValid = !selectedMixRatios.isEmpty();
        // 通知Activity更新下一步按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).enableNextButton(isValid);
        }
    }

    @Override
    public void onMixRatioSelected(MixRatio mixRatio) {
        if (selectedMixRatios.contains(mixRatio)) {
            selectedMixRatios.remove(mixRatio);
        } else {
            selectedMixRatios.add(mixRatio);
        }
        checkInputValidity();
    }

    @Override
    public void onMixRatioDelete(MixRatio mixRatio) {
        // 创建确认删除的对话框
        new AlertDialog.Builder(requireContext())
            .setTitle("删除配合比")
            .setMessage("确定要删除配合比 \"" + mixRatio.getName() + "\" 吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                // 在后台线程执行删除操作
                executorService.execute(() -> {
                    try {
                        // 删除配合比
                        databaseHelper.mixRatioDao().delete(mixRatio);
                        
                        // 在主线程更新UI
                        requireActivity().runOnUiThread(() -> {
                            // 从适配器中移除
                            mixRatioAdapter.removeMixRatio(mixRatio);
                            
                            // 显示删除成功的提示
                            Snackbar.make(requireView(), "配合比删除成功", Snackbar.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        // 在主线程显示错误提示
                        requireActivity().runOnUiThread(() -> {
                            Log.e("SelectMixRatioFragment", "删除配合比失败", e);
                            Snackbar.make(requireView(), "删除配合比失败：" + e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                        });
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMixRatios();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    public List<MixRatio> getSelectedMixRatios() {
        return new ArrayList<>(selectedMixRatios);
    }
}