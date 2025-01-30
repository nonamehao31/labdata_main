package com.example.labdata_main;

import android.os.Bundle;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.adapter.MoldingMethodAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.fragment.CompactionMethodFragment;
import com.example.labdata_main.fragment.MixingMethodFragment;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SelectMoldingMethodFragment extends Fragment implements MixingMethodFragment.OnNextStepListener {

    private RecyclerView rvMoldingMethods;
    private TextView emptyView;
    private MaterialCardView cardAddMoldingMethod;
    private MoldingMethodAdapter moldingMethodAdapter;
    private List<MoldingMethod> moldingMethods = new ArrayList<>();
    private ViewPager2 viewPager;
    private List<MoldingMethod> selectedMethods = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_molding_method, container, false);

        initViews(view);
        setupListeners();
        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        emptyView = view.findViewById(R.id.emptyView_at_molding);
        cardAddMoldingMethod = view.findViewById(R.id.cardAddMoldingMethod);
    }

    private void setupListeners() {
        cardAddMoldingMethod.setOnClickListener(v -> showMoldingMethodBottomSheet());
    }

    private void setupRecyclerView() {
        moldingMethodAdapter = new MoldingMethodAdapter(
            moldingMethods, 
            this::onDeleteMoldingMethod,
            this::onMoldingMethodSelectionChanged
        );
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMoldingMethods.setAdapter(moldingMethodAdapter);
        updateEmptyView();
    }

    private void onMoldingMethodSelectionChanged(List<MoldingMethod> methods) {
        selectedMethods.clear();
        selectedMethods.addAll(methods);
        // TODO: 根据需要处理选中状态变化
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
                AppDatabase.getInstance(requireContext())
                    .moldingMethodDao()
                    .delete(method);

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
        moldingMethods.add(method);
        moldingMethodAdapter.notifyItemInserted(moldingMethods.size() - 1);
        updateEmptyView();
    }

    private void updateEmptyView() {
        emptyView.setVisibility(moldingMethods.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onNextStep() {
        // 这是一个空实现，因为实际的页面切换逻辑已经在 ViewPagerAdapter 中处理
    }

    public List<MoldingMethod> getSelectedMethods() {
        return new ArrayList<>(selectedMethods);
    }
}
