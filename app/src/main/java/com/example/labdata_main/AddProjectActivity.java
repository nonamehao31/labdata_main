package com.example.labdata_main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddProjectActivity extends BaseActivity {
    private TextInputEditText etProjectName;
    private TextInputEditText etDeadline;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        // 初始化日期格式化工具
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etProjectName = findViewById(R.id.etProjectName);
        etDeadline = findViewById(R.id.etDeadline);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        // 设置返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 设置日期选择框点击事件
        etDeadline.setOnClickListener(v -> showDatePicker());

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveProject());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDeadline.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveProject() {
        // 获取输入的项目名称和截止日期
        String projectName = etProjectName.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(projectName)) {
            Toast.makeText(this, "请输入项目名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(deadline)) {
            Toast.makeText(this, "请选择截止日期", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建新的项目对象
        Project project = new Project(projectName, deadline);

        // 保存到数据库
        try {
            long id = databaseHelper.insertProject(project);
            if (id != -1) {
                project.setId((int) id);
                Toast.makeText(this, "项目保存成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
