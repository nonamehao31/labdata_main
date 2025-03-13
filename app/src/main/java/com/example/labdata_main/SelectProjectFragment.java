package com.example.labdata_main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.labdata_main.adapter.ProjectSelectionAdapter;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.service.ProjectService;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class SelectProjectFragment extends Fragment implements 
        ProjectSelectionAdapter.OnProjectSelectedListener,
        AddProjectBottomSheet.OnProjectAddedListener {
    
    private RecyclerView rvProjects;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProjectSelectionAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ProjectService projectService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());
        projectService = new ProjectService(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_project, container, false);

        rvProjects = view.findViewById(R.id.rvProjects);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        MaterialCardView addProjectCard = view.findViewById(R.id.addProjectCard);

        setupRecyclerView();
        
        // 设置下拉刷新
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadProjects);
        }
        
        // 设置添加项目卡片点击事件
        addProjectCard.setOnClickListener(v -> {
            AddProjectBottomSheet bottomSheet = AddProjectBottomSheet.newInstance();
            bottomSheet.setOnProjectAddedListener(this);
            bottomSheet.show(getChildFragmentManager(), "AddProjectBottomSheet");
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProjects();
    }

    private void setupRecyclerView() {
        adapter = new ProjectSelectionAdapter(this);
        rvProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProjects.setAdapter(adapter);
    }

    private void loadProjects() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // 使用ProjectService从后端加载项目
        projectService.getProjects(projects -> {
            if (isAdded()) {
                mainHandler.post(() -> {
                    adapter.submitList(new ArrayList<>(projects));
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onProjectSelected(Project project) {
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ExperimentTaskSetupActivity activity = (ExperimentTaskSetupActivity) getActivity();
            activity.setSelectedProject(project);
        }
    }

    @Override
    public void onProjectDeleteRequested(Project project) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除项目")
                .setMessage("确定要删除项目 \"" + project.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 显示加载提示
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                    
                    // 使用ProjectService删除项目
                    projectService.deleteProject(project, (success, message) -> {
                        if (isAdded()) {
                            mainHandler.post(() -> {
                                if (swipeRefreshLayout != null) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                                
                                // 显示操作结果
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                
                                if (success) {
                                    // 删除成功后刷新列表
                                    loadProjects();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onProjectAdded(Project project) {
        // 项目添加成功后刷新列表
        loadProjects();
    }
}
