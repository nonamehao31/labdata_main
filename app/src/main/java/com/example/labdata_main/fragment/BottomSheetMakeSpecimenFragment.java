package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetMakeSpecimenFragment extends BottomSheetDialogFragment {
    private ExperimentTask task;
    private ViewPager2 viewPager;
    private View step1Layout, step2Layout;
    private TextView step1Circle, step2Circle;
    private TextView step1Text, step2Text;
    private View step1Line;

    public static BottomSheetMakeSpecimenFragment newInstance(ExperimentTask task) {
        BottomSheetMakeSpecimenFragment fragment = new BottomSheetMakeSpecimenFragment();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable("task");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_make_specimen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        initViews(view);
        // 设置ViewPager
        setupViewPager();
        // 设置步骤导航点击事件
        setupStepClickListeners();
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        step1Layout = view.findViewById(R.id.step1Layout);
        step2Layout = view.findViewById(R.id.step2Layout);
        step1Circle = view.findViewById(R.id.step1Circle);
        step2Circle = view.findViewById(R.id.step2Circle);
        step1Text = view.findViewById(R.id.step1Text);
        step2Text = view.findViewById(R.id.step2Text);
        step1Line = view.findViewById(R.id.step1Line);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? ScanDeviceFragment.newInstance(task) 
                                   : GenerateSpecimenFragment.newInstance();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepIndicators(position);
            }
        });
    }

    private void setupStepClickListeners() {
        step1Layout.setOnClickListener(v -> viewPager.setCurrentItem(0));
        step2Layout.setOnClickListener(v -> viewPager.setCurrentItem(1));
    }

    private void updateStepIndicators(int position) {
        // 更新第一步的状态
        step1Circle.setBackgroundResource(position == 0 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step1Circle.setTextColor(getResources().getColor(position == 0 ? 
            android.R.color.white : R.color.step_text_inactive));
        step1Text.setTextColor(getResources().getColor(position == 0 ? 
            R.color.step_text_active : R.color.step_text_inactive));

        // 更新连接线
        step1Line.setBackgroundResource(position == 1 ? 
            R.color.step_line_active : R.color.step_line_inactive);

        // 更新第二步的状态
        step2Circle.setBackgroundResource(position == 1 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step2Circle.setTextColor(getResources().getColor(position == 1 ? 
            android.R.color.white : R.color.step_text_inactive));
        step2Text.setTextColor(getResources().getColor(position == 1 ? 
            R.color.step_text_active : R.color.step_text_inactive));
    }
}
