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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinterManager implements PrinterManager {
    private static final String TAG = "BluetoothPrinterManager";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice printer;
    private final Handler mainHandler;

    public BluetoothPrinterManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean isPrinterServiceEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public List<PrinterInfo> getPairedPrinters() {
        List<PrinterInfo> printers = new ArrayList<>();
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices != null) {
                        for (BluetoothDevice device : pairedDevices) {
                            printers.add(PrinterInfo.fromBluetoothDevice(device));
                            
                            // 打印设备列表的日志，帮助调试
                            String deviceName = device.getName() != null ? device.getName() : "未知设备";
                            String deviceAddress = device.getAddress();
                            Log.d(TAG, "找到已配对的蓝牙设备: " + deviceName + " (" + deviceAddress + ")");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取已配对设备失败", e);
                }
            } else {
                Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法获取配对设备");
                Toast.makeText(context, "缺少蓝牙连接权限，请在应用设置中授予权限", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "设备不支持蓝牙");
            Toast.makeText(context, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }
        return printers;
    }

    @Override
    public void connectToPrinter(PrinterInfo printerInfo, PrintCallback callback) {
        if (printerInfo.getType() != PrinterInfo.TYPE_BLUETOOTH) {
            callback.onPrintError("不是蓝牙打印机");
            return;
        }
        
        BluetoothDevice device = printerInfo.getBluetoothDevice();
        if (device == null) {
            callback.onPrintError("蓝牙设备信息无效");
            return;
        }
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "缺少蓝牙连接权限，无法连接打印机");
            callback.onPrintError("缺少蓝牙连接权限");
            return;
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "正在连接打印机: " + (device.getName() != null ? device.getName() : device.getAddress()));
                
                // 首先尝试断开现有连接
                if (bluetoothSocket != null) {
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        Log.w(TAG, "关闭现有连接时发生错误", e);
                    }
                }
                
                // 创建并连接新的Socket
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                
                // 确保发现过程未在进行中
                if (bluetoothAdapter.isDiscovering()) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
                
                // 尝试连接并设置超时
                bluetoothSocket.connect();
                printer = device;
                mainHandler.post(() -> {
                    Toast.makeText(context, "已连接到打印机: " + device.getName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "成功连接到打印机: " + device.getName());
                });
            } catch (IOException e) {
                Log.e(TAG, "连接打印机失败: " + e.getMessage(), e);
                try {
                    // 尝试回退方法连接
                    Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                    bluetoothSocket = (BluetoothSocket) m.invoke(device, 1);
                    bluetoothSocket.connect();
                    printer = device;
                    mainHandler.post(() -> {
                        Toast.makeText(context, "使用备选方法连接到打印机: " + device.getName(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "使用备选方法成功连接到打印机: " + device.getName());
                    });
                } catch (Exception e2) {
                    Log.e(TAG, "备选连接方法也失败: " + e2.getMessage(), e2);
                    mainHandler.post(() -> callback.onPrintError("连接打印机失败: " + e.getMessage()));
                }
            }
        }).start();
    }

    @Override
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
                byte[] init = {0x1B, 0x40};
                outputStream.write(init);
                
                // 设置对齐方式为居中
                byte[] align = {0x1B, 0x61, 0x01};
                outputStream.write(align);
                
                // 转换位图为打印数据
                byte[] imageData = convertBitmapToCommandData(qrCode);
                outputStream.write(imageData);
                
                // 走纸并切纸
                byte[] feed = {0x1D, 0x56, 0x42, 0x00};
                outputStream.write(feed);
                
                outputStream.flush();
                
                mainHandler.post(() -> {
                    callback.onPrintSuccess();
                    Toast.makeText(context, "打印成功", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                Log.e(TAG, "打印失败", e);
                mainHandler.post(() -> callback.onPrintError("打印失败: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * 将位图转换为ESC/POS打印命令数据
     */
    private byte[] convertBitmapToCommandData(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // 调整分辨率以适应打印机
        int adjustedWidth = Math.min(width, 384); // 大多数热敏打印机宽度为384点
        float ratio = (float) adjustedWidth / width;
        int adjustedHeight = (int) (height * ratio);
        
        // 缩放位图
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, adjustedWidth, adjustedHeight, true);
        
        // 转换为二值图像
        int[] pixels = new int[adjustedWidth * adjustedHeight];
        scaledBitmap.getPixels(pixels, 0, adjustedWidth, 0, 0, adjustedWidth, adjustedHeight);
        
        // 计算每行所需的字节数（每8个像素一个字节）
        int bytesPerRow = (adjustedWidth + 7) / 8;
        byte[] data = new byte[bytesPerRow * adjustedHeight + 8];
        
        // ESC * 命令，用于位图打印
        data[0] = 0x1D;
        data[1] = 0x76;
        data[2] = 0x30;
        data[3] = 0x00;
        data[4] = (byte) (bytesPerRow & 0xFF);
        data[5] = (byte) ((bytesPerRow >> 8) & 0xFF);
        data[6] = (byte) (adjustedHeight & 0xFF);
        data[7] = (byte) ((adjustedHeight >> 8) & 0xFF);
        
        // 转换像素数据为打印数据
        int index = 8;
        for (int y = 0; y < adjustedHeight; y++) {
            for (int x = 0; x < adjustedWidth; x += 8) {
                byte b = 0;
                for (int i = 0; i < 8; i++) {
                    if (x + i < adjustedWidth) {
                        int color = pixels[y * adjustedWidth + x + i];
                        if ((color & 0xFF) < 128) { // 简单阈值，黑色为1，白色为0
                            b |= (1 << (7 - i));
                        }
                    }
                }
                data[index++] = b;
            }
        }
        
        return data;
    }
    
    /**
     * 根据蓝牙地址获取设备
     * @param address 蓝牙地址
     * @return 蓝牙设备对象，如果未找到则返回null
     */
    public BluetoothDevice getDeviceByAddress(String address) {
        if (bluetoothAdapter == null) {
            return null;
        }
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getAddress().equals(address)) {
                        return device;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public void disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
                printer = null;
                Log.d(TAG, "已断开与打印机的连接");
            } catch (IOException e) {
                Log.e(TAG, "断开连接时发生错误", e);
            }
        }
    }
}
