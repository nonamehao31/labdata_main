package com.example.labdata_main.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

import com.example.labdata_main.model.MaterialItem;

import java.util.List;

public class PieChartView extends View {
    private Paint paint;
    private RectF rectF;
    private List<MaterialItem> materials;
    private int[] colors = {
        Color.parseColor("#FF6384"),
        Color.parseColor("#36A2EB"),
        Color.parseColor("#FFCE56"),
        Color.parseColor("#4BC0C0"),
        Color.parseColor("#9966FF")
    };

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();
    }

    public void setMaterials(List<MaterialItem> materials) {
        this.materials = materials;
        invalidate(); // 请求重绘
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (materials == null || materials.isEmpty()) {
            Log.d("PieChartView", "No materials to draw");
            return;
        }

        Log.d("PieChartView", "Total materials: " + materials.size());
        float total = 0;
        for (MaterialItem material : materials) {
            Log.d("PieChartView", "Material: " + material.getName() + ", Amount: " + material.getAmount());
            if (material.getAmount() != null && !material.getAmount().trim().isEmpty()) {
                try {
                    total += Float.parseFloat(material.getAmount().trim());
                } catch (NumberFormatException e) {
                    Log.e("PieChartView", "Invalid amount: " + material.getAmount());
                }
            }
        }

        Log.d("PieChartView", "Total amount: " + total);
        if (total <= 0) {
            // 如果没有有效的数量，尝试使用百分比
            total = 0;
            for (MaterialItem material : materials) {
                if (material.getPercentage() > 0) {
                    total += material.getPercentage();
                }
            }
            
            if (total <= 0) {
                Log.d("PieChartView", "Total amount is zero or negative");
                return;
            }
        }

        // 确保绘制区域是正方形
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = size / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = 0;
        for (int i = 0; i < materials.size(); i++) {
            MaterialItem material = materials.get(i);
            float sweepAngle = 0;
            
            if (material.getAmount() != null && !material.getAmount().trim().isEmpty()) {
                try {
                    sweepAngle = (Float.parseFloat(material.getAmount().trim()) / total) * 360;
                } catch (NumberFormatException e) {
                    Log.e("PieChartView", "Invalid material amount: " + material.getAmount());
                }
            } else if (material.getPercentage() > 0) {
                sweepAngle = material.getPercentage() / total * 360;
            }

            paint.setColor(colors[i % colors.length]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 200;
        int desiredHeight = 200;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // 根据不同的测量模式选择宽度
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        // 根据不同的测量模式选择高度
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        // 确保是正方形
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }
}
