package com.example.labdata_main.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * WiFi打印机管理器，通过网络连接打印机
 */
public class WiFiPrinterManager implements PrinterManager {
    private Context context;
    private Handler mainHandler;
    private PrinterInfo currentPrinter;
    private Socket printerSocket;
    private OutputStream outputStream;
    private PrinterDiscoveryManager discoveryManager;

    public WiFiPrinterManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.discoveryManager = new PrinterDiscoveryManager(context);
    }

    /**
     * 连接到打印机
     * @param printer 打印机信息
     * @param callback 打印回调
     */
    @Override
    public void connectToPrinter(PrinterInfo printer, PrintCallback callback) {
        if (printer == null || printer.getType() != PrinterInfo.TYPE_WIFI) {
            if (callback != null) {
                callback.onPrintError("无效的WiFi打印机");
            }
            return;
        }
        
        this.currentPrinter = printer;
        
        // 在后台线程中连接打印机
        new Thread(() -> {
            if (callback != null) {
                mainHandler.post(callback::onPrintStart);
            }
            
            try {
                // 如果已有连接，先断开
                disconnect();
                
                // 创建新连接
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(printer.getAddress(), printer.getPort()), 5000);
                printerSocket = socket;
                outputStream = socket.getOutputStream();
                
                if (callback != null) {
                    mainHandler.post(callback::onPrintSuccess);
                }
            } catch (Exception e) {
                Log.e("WiFiPrinterManager", "连接打印机失败", e);
                if (callback != null) {
                    final String errorMsg = e.getMessage();
                    mainHandler.post(() -> callback.onPrintError(errorMsg));
                }
            }
        }).start();
    }

    /**
     * 打印二维码
     * @param qrCode 二维码图像
     * @param callback 打印回调
     */
    @Override
    public void printQRCode(Bitmap qrCode, PrintCallback callback) {
        if (outputStream == null) {
            if (callback != null) {
                callback.onPrintError("未连接到打印机");
            }
            return;
        }
        
        if (qrCode == null) {
            if (callback != null) {
                callback.onPrintError("二维码图像无效");
            }
            return;
        }
        
        new Thread(() -> {
            if (callback != null) {
                mainHandler.post(callback::onPrintStart);
            }
            
            try {
                byte[] printData = generatePrintData(qrCode);
                outputStream.write(printData);
                outputStream.flush();
                
                if (callback != null) {
                    mainHandler.post(callback::onPrintSuccess);
                }
            } catch (Exception e) {
                Log.e("WiFiPrinterManager", "打印失败", e);
                if (callback != null) {
                    final String errorMsg = e.getMessage();
                    mainHandler.post(() -> callback.onPrintError(errorMsg));
                }
            }
        }).start();
    }

    /**
     * 生成打印数据
     * @param qrCode 二维码图像
     * @return 打印数据
     */
    private byte[] generatePrintData(Bitmap qrCode) {
        // 这里生成打印机特定的打印数据
        // 不同型号的打印机可能需要不同的数据格式
        
        try {
            // 将Bitmap转换为TSC打印命令
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 初始化标签大小（单位：mm）
            int width = 50;
            int height = 30;
            
            // TSC标签初始化命令
            String initCommand = "SIZE " + width + " mm, " + height + " mm\n" +
                    "GAP 2 mm, 0 mm\n" +
                    "DIRECTION 1\n" +
                    "REFERENCE 0, 0\n" +
                    "SPEED 4\n" +
                    "DENSITY 8\n" +
                    "SET TEAR ON\n" +
                    "CLS\n";
            outputStream.write(initCommand.getBytes());
            
            // 将二维码图像转为1位黑白图像
            Bitmap monoQRCode = qrCode;
            if (qrCode.getConfig() != Bitmap.Config.ARGB_8888) {
                monoQRCode = qrCode.copy(Bitmap.Config.ARGB_8888, true);
            }
            
            int qrWidth = monoQRCode.getWidth();
            int qrHeight = monoQRCode.getHeight();
            
            // 定义二维码在标签上的位置（居中）
            int qrX = (width * 8 - qrWidth) / 2;  // 8是点密度转换因子
            int qrY = (height * 8 - qrHeight) / 4;  // 使二维码偏上一些
            
            // 将二维码转为ZPL DMATRIX命令
            StringBuilder qrCommand = new StringBuilder();
            qrCommand.append("BITMAP ").append(qrX).append(",").append(qrY).append(",")
                    .append(qrWidth).append(",").append(qrHeight).append(",1,");
            
            int[] pixels = new int[qrWidth * qrHeight];
            monoQRCode.getPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrHeight);
            
            // 转换每一行像素数据
            for (int y = 0; y < qrHeight; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    int pixel = pixels[y * qrWidth + x];
                    int alpha = Color.alpha(pixel);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    int gray = (red + green + blue) / 3;
                    
                    if (alpha > 128 && gray < 128) {
                        qrCommand.append("1");
                    } else {
                        qrCommand.append("0");
                    }
                }
                qrCommand.append("\n");
            }
            
            outputStream.write(qrCommand.toString().getBytes());
            
            // 添加文本
            outputStream.write(("TEXT 50,200,\"3\",0,1,1,\"扫描二维码获取试件信息\"\n").getBytes());
            
            // 打印命令
            outputStream.write("PRINT 1,1\n".getBytes());
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.e("WiFiPrinterManager", "生成打印数据失败", e);
            return new byte[0];
        }
    }

    /**
     * 开始发现打印机
     * @param listener 发现监听器
     */
    public void discoverPrinters(PrinterDiscoveryManager.PrinterDiscoveryListener listener) {
        // 设置监听器并开始发现
        discoveryManager.setPrinterDiscoveryListener(listener);
        discoveryManager.startDiscovery();
    }

    /**
     * 停止发现打印机
     */
    public void stopDiscovery() {
        discoveryManager.stopDiscovery();
    }
    
    /**
     * 检查打印服务是否启用
     * @return 服务是否启用
     */
    @Override
    public boolean isPrinterServiceEnabled() {
        // 对于WiFi打印机，检查网络服务是否可用
        return true; // 简化实现，假设WiFi服务始终可用
    }

    /**
     * 断开连接
     */
    @Override
    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            
            if (printerSocket != null) {
                printerSocket.close();
                printerSocket = null;
            }
        } catch (IOException e) {
            Log.e("WiFiPrinterManager", "断开连接失败", e);
        }
    }
}
