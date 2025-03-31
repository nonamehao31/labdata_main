package com.example.labdata_main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片裁剪Activity
 * 使用UCrop库进行图片裁剪操作
 */
public class CropImageActivity extends AppCompatActivity {

    private static final String TAG = "CropImageActivity";
    private Uri inputUri;
    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 获取传入的参数
        String inputUriString = getIntent().getStringExtra("inputUri");
        String outputUriString = getIntent().getStringExtra("outputUri");
        int aspectRatioX = getIntent().getIntExtra("aspectRatioX", 1);
        int aspectRatioY = getIntent().getIntExtra("aspectRatioY", 1);
        int outputWidth = getIntent().getIntExtra("outputWidth", 300);
        int outputHeight = getIntent().getIntExtra("outputHeight", 300);
        
        if (inputUriString == null || outputUriString == null) {
            Toast.makeText(this, "图片URI错误", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        
        inputUri = Uri.parse(inputUriString);
        outputUri = Uri.parse(outputUriString);
        
        // 检查输入文件是否可读
        try {
            InputStream inputStream = getContentResolver().openInputStream(inputUri);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "无法读取输入文件: " + e.getMessage());
            Toast.makeText(this, "无法读取选中的图片", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        
        // 使用UCrop进行裁剪
        UCrop uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(aspectRatioX, aspectRatioY)
                .withMaxResultSize(outputWidth, outputHeight);
        
        // 设置UCrop的选项
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90); // 设置压缩质量
        options.setHideBottomControls(false); // 显示底部控制栏
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarTitle("裁剪头像");
        
        uCrop.withOptions(options);
        
        try {
            uCrop.start(CropImageActivity.this);
        } catch (Exception e) {
            Log.e(TAG, "启动UCrop时出错: " + e.getMessage());
            Toast.makeText(this, "无法启动裁剪工具", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri resultUri = UCrop.getOutput(data);
                // 裁剪成功，返回结果
                Intent resultIntent = new Intent();
                resultIntent.putExtra("croppedUri", resultUri.toString());
                setResult(RESULT_OK, resultIntent);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                Log.e(TAG, "裁剪错误: " + (cropError != null ? cropError.getMessage() : "未知错误"));
                Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
            } else {
                // 用户取消
                setResult(RESULT_CANCELED);
            }
            
            finish();
        }
    }
}
