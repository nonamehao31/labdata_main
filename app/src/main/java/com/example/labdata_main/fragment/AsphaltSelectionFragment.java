package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.R;
import com.example.labdata_main.adapter.AsphaltAdapter;
import com.example.labdata_main.dialog.AddAsphaltBottomSheetDialog;
import com.example.labdata_main.model.AsphaltInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AsphaltSelectionFragment extends Fragment implements AddAsphaltBottomSheetDialog.OnAsphaltAddedListener {
    private static final String ARG_TASK_NAME = "task_name";
    private String taskName;
    private RecyclerView rvAsphalt;
    private AsphaltAdapter asphaltAdapter;
    private MaterialCardView addAsphaltCard;
    private MaterialButton btnNext;
    private ViewPager2 viewPager;
    private List<AsphaltInfo> asphaltList = new ArrayList<>();

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
        setupAddAsphaltCard();
        setupNextButton();
    }

    private void initializeViews(View view) {
        rvAsphalt = view.findViewById(R.id.rvAsphalt);
        addAsphaltCard = view.findViewById(R.id.addAsphaltCard);
        btnNext = view.findViewById(R.id.btnNext);
    }

    private void setupRecyclerView() {
        asphaltAdapter = new AsphaltAdapter(asphalt -> {
            // 处理点击事件，切换选中状态
            asphaltAdapter.toggleSelection(asphalt);
            updateNextButtonState();
        });
        
        rvAsphalt.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAsphalt.setAdapter(asphaltAdapter);
        updateAsphaltList();
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
        asphaltList.add(asphaltInfo);
        updateAsphaltList();
    }

    private void updateAsphaltList() {
        List<AsphaltInfo> newList = new ArrayList<>(asphaltList);
        asphaltAdapter.submitList(newList);
    }

    public boolean hasAsphaltSelected() {
        return !asphaltAdapter.getSelectedAsphalt().isEmpty();
    }

    public Set<AsphaltInfo> getSelectedAsphalt() {
        return asphaltAdapter.getSelectedAsphalt();
    }
}
