package com.example.labdata_main.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.adapter.ExperimentTypeAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import java.util.Set;

public class ExperimentSelectionBottomSheetDialog extends BottomSheetDialogFragment {
    private ExperimentTypeAdapter adapter;
    private OnExperimentsSelectedListener listener;

    public interface OnExperimentsSelectedListener {
        void onExperimentsSelected(Set<ExperimentType> selectedExperiments);
    }

    public void setOnExperimentsSelectedListener(OnExperimentsSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_experiment_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvExperiments);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        adapter = new ExperimentTypeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // 加载实验类型
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            adapter.submitList(db.experimentTypeDao().getExperimentTypesByCategory("ASPHALT"));
        }).start();

        btnConfirm.setOnClickListener(v -> {
            Set<ExperimentType> selectedExperiments = adapter.getSelectedExperiments();
            if (selectedExperiments.isEmpty()) {
                Toast.makeText(requireContext(), "请至少选择一个实验", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) {
                listener.onExperimentsSelected(selectedExperiments);
            }
            dismiss();
        });
    }
}
