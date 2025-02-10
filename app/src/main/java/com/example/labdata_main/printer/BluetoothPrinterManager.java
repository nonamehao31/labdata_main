package com.example.labdata_main.printer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinterManager {
    private static final String TAG = "BluetoothPrinterManager";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice printer;
    private final Handler mainHandler;

    public interface PrintCallback {
        void onPrintStart();
        void onPrintSuccess();
        void onPrintError(String error);
    }

    public BluetoothPrinterManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedPrinters() {
        List<BluetoothDevice> printers = new ArrayList<>();
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices != null) {
                    printers.addAll(pairedDevices);
                }
            }
        }
        return printers;
    }

    public void connectToPrinter(BluetoothDevice device, PrintCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            callback.onPrintError("缺少蓝牙连接权限");
            return;
        }

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothSocket.connect();
                printer = device;
                mainHandler.post(() -> Toast.makeText(context, "已连接到打印机: " + device.getName(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                Log.e(TAG, "连接打印机失败", e);
                mainHandler.post(() -> callback.onPrintError("连接打印机失败: " + e.getMessage()));
            }
        }).start();
    }

    public void printQRCode(Bitmap qrCode, PrintCallback callback) {
        if (bluetoothSocket == null || printer == null) {
            callback.onPrintError("未连接打印机");
            return;
        }

        new Thread(() -> {
            try {
                mainHandler.post(callback::onPrintStart);

                OutputStream outputStream = bluetoothSocket.getOutputStream();

                // 初始化打印机
                outputStream.write(new byte[]{0x1B, 0x40}); // ESC @

                // 设置打印模式（8点单密度）
                outputStream.write(new byte[]{0x1B, 0x4B, 0x08});

                // 将位图数据转换为打印机可以理解的格式
                byte[] printData = convertBitmapToBytes(qrCode);
                outputStream.write(printData);

                // 打印并换行
                outputStream.write(new byte[]{0x0A});
                outputStream.flush();

                mainHandler.post(callback::onPrintSuccess);
            } catch (IOException e) {
                Log.e(TAG, "打印失败", e);
                mainHandler.post(() -> callback.onPrintError("打印失败: " + e.getMessage()));
            }
        }).start();
    }

    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // 将图像数据转换为打印机可以理解的格式
        byte[] data = new byte[width * height / 8 + 100];
        int offset = 0;

        // 打印机命令头
        data[offset++] = 0x1D;
        data[offset++] = 0x76;
        data[offset++] = 0x30;
        data[offset++] = 0x00;

        // 宽度和高度
        data[offset++] = (byte) (width / 8);
        data[offset++] = 0x00;
        data[offset++] = (byte) (height / 8);
        data[offset++] = 0x00;

        // 转换图像数据
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte b = 0;
                for (int k = 0; k < 8; k++) {
                    if (x + k < width) {
                        int pixel = pixels[y * width + x + k];
                        if ((pixel & 0xFF) < 128) {
                            b |= (1 << (7 - k));
                        }
                    }
                }
                data[offset++] = b;
            }
        }

        // 创建新的字节数组，仅包含有效数据
        byte[] result = new byte[offset];
        System.arraycopy(data, 0, result, 0, offset);
        return result;
    }

    public void disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭蓝牙连接失败", e);
            }
            bluetoothSocket = null;
            printer = null;
        }
    }
}
