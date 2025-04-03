package com.example.labdata_main.printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * WiFi打印机管理器，通过网络连接打印机
 */
public class WiFiPrinterManager implements PrinterManager {
    private static final String TAG = "WiFiPrinterManager";
    private Context context;
    private Handler mainHandler;
    private PrinterInfo currentPrinter;
    private Socket printerSocket;
    private OutputStream outputStream;
    private PrinterDiscoveryManager discoveryManager;

    // 权限请求码
    private static final int REQUEST_WIFI_PERMISSION = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;

    public WiFiPrinterManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.discoveryManager = new PrinterDiscoveryManager(context);
        Log.d(TAG, "WiFiPrinterManager初始化完成");
    }

    /**
     * 连接到打印机
     * @param printer 打印机信息
     * @param callback 打印回调
     */
    @Override
    public void connectToPrinter(PrinterInfo printer, PrintCallback callback) {
        Log.d(TAG, "尝试连接到WiFi打印机: " + printer.getName());
        
        if (printer == null || printer.getAddress() == null || printer.getAddress().isEmpty()) {
            Log.e(TAG, "打印机信息无效");
            if (callback != null) {
                callback.onPrintError("打印机信息无效");
            }
            return;
        }
        
        this.currentPrinter = printer;
        
        // 在后台线程中连接打印机
        new Thread(() -> {
            if (callback != null) {
                Log.d(TAG, "通知UI开始连接打印机");
                mainHandler.post(callback::onPrintStart);
            }
            
            // 尝试连接不同的端口
            boolean connected = false;
            Exception lastException = null;
            
            // 要尝试的端口列表
            int[] portsToTry = {printer.getPort(), 9100, 631, 515};
            
            // 确保不重复尝试相同的端口
            portsToTry = removeDuplicates(portsToTry);
            
            for (int port : portsToTry) {
                try {
                    // 如果已有连接，先断开
                    disconnect();
                    
                    // 创建新连接
                    Log.d(TAG, "尝试连接端口: " + printer.getAddress() + ":" + port);
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(printer.getAddress(), port), 10000); // 10秒超时
                    printerSocket = socket;
                    outputStream = socket.getOutputStream();
                    
                    // 如果不是原始端口，更新打印机端口信息
                    if (port != printer.getPort()) {
                        Log.d(TAG, "更新打印机端口从 " + printer.getPort() + " 到 " + port);
                        printer.setPort(port);
                        currentPrinter = printer;
                    }
                    
                    connected = true;
                    Log.d(TAG, "成功连接到WiFi打印机");
                    break; // 成功连接，跳出循环
                } catch (Exception e) {
                    Log.e(TAG, "连接端口 " + port + " 失败: " + e.getMessage());
                    lastException = e;
                    // 继续尝试下一个端口
                }
            }
            
            if (connected) {
                if (callback != null) {
                    Log.d(TAG, "通知UI连接成功");
                    mainHandler.post(callback::onPrintSuccess);
                }
            } else {
                if (callback != null) {
                    final String errorMsg = lastException != null ? lastException.getMessage() : "无法连接到打印机";
                    Log.d(TAG, "通知UI连接失败: " + errorMsg);
                    mainHandler.post(() -> callback.onPrintError(errorMsg));
                }
            }
        }).start();
    }
    
    /**
     * 移除数组中的重复元素
     * @param array 原始数组
     * @return 去重后的数组
     */
    private int[] removeDuplicates(int[] array) {
        // 使用Set去重
        java.util.Set<Integer> set = new java.util.HashSet<>();
        for (int value : array) {
            set.add(value);
        }
        
        // 转回数组
        int[] result = new int[set.size()];
        int index = 0;
        for (int value : set) {
            result[index++] = value;
        }
        
        return result;
    }

    /**
     * 开始发现打印机
     * @param listener 发现监听器
     */
    public void discoverPrinters(PrinterDiscoveryManager.PrinterDiscoveryListener listener) {
        Log.d(TAG, "开始搜索WiFi打印机");
        
        if (listener == null) {
            Log.e(TAG, "发现监听器为null，终止搜索");
            return;
        }
        
        // 检查WiFi访问权限
        if (!checkWifiPermission()) {
            Log.e(TAG, "没有WIFI访问权限，无法搜索WiFi打印机");
            listener.onDiscoveryError("没有WiFi访问权限，请在应用设置中授予权限");
            return;
        }
        
        // 包装监听器，增加详细日志
        PrinterDiscoveryManager.PrinterDiscoveryListener wrappedListener = new PrinterDiscoveryManager.PrinterDiscoveryListener() {
            @Override
            public void onPrinterDiscoveryStarted() {
                Log.d(TAG, "打印机发现过程开始");
                listener.onPrinterDiscoveryStarted();
            }
            
            @Override
            public void onPrinterFound(PrinterInfo printer) {
                Log.d(TAG, "发现打印机: " + printer.getName() + " (" + printer.getAddress() + ")");
                listener.onPrinterFound(printer);
            }

            @Override
            public void onPrinterDiscoveryFinished(List<PrinterInfo> printers) {
                Log.d(TAG, "打印机发现完成，共发现 " + printers.size() + " 台打印机");
                listener.onPrinterDiscoveryFinished(printers);
            }

            @Override
            public void onDiscoveryError(String errorMessage) {
                Log.e(TAG, "打印机发现出错: " + errorMessage);
                listener.onDiscoveryError(errorMessage);
            }
        };
        
        // 检查网络状态和当前WiFi连接
        String wifiSsid = "";
        boolean isPrinterHotspot = false;
        String gatewayAddress = null;
        boolean isWifiConnected = false;
        
        try {
            Log.d(TAG, "检查网络状态");
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        isWifiConnected = true;
                        Log.d(TAG, "设备已连接到WiFi网络");
                        
                        // 获取WiFi网络信息
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            if (wifiInfo != null) {
                                int ipAddress = wifiInfo.getIpAddress();
                                String ip = String.format("%d.%d.%d.%d",
                                        (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                                        (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                                Log.d(TAG, "当前WiFi: " + wifiInfo.getSSID() + ", IP: " + ip);
                                
                                // 获取SSID并检查是否为打印机热点
                                wifiSsid = wifiInfo.getSSID().replace("\"", "");
                                isPrinterHotspot = isPrinterHotspotSSID(wifiSsid);
                                
                                if (isPrinterHotspot) {
                                    Log.d(TAG, "检测到当前WiFi可能是打印机热点: " + wifiSsid);
                                }
                                
                                // 获取网关地址
                                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                                if (dhcpInfo != null) {
                                    int gateway = dhcpInfo.gateway;
                                    gatewayAddress = String.format("%d.%d.%d.%d",
                                            (gateway & 0xff), (gateway >> 8 & 0xff),
                                            (gateway >> 16 & 0xff), (gateway >> 24 & 0xff));
                                    Log.d(TAG, "当前网关地址: " + gatewayAddress);
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "设备未连接到WiFi网络，连接到了其他类型的网络，可能会影响打印机发现");
                    }
                } else {
                    Log.w(TAG, "无网络连接，可能无法发现WiFi打印机");
                    wrappedListener.onDiscoveryError("未连接到网络，无法搜索WiFi打印机");
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查网络状态时出错", e);
        }
        
        // 如果连接到打印机热点，直接使用网关地址
        if (isPrinterHotspot && gatewayAddress != null) {
            Log.d(TAG, "检测到打印机热点，使用网关地址: " + gatewayAddress);
            
            // 创建一个表示热点打印机的对象
            final String hotspotName = wifiSsid;
            final String hotspotGateway = gatewayAddress;
            
            // 创建两个最常用端口的打印机配置
            // 9100是大多数打印机的打印端口，631是IPP标准端口
            createAndTestPrinterWithPorts(hotspotName, hotspotGateway, new int[]{9100, 631}, wrappedListener);
        }
        
        // 启动标准的打印机发现过程
        Log.d(TAG, "启动打印机发现管理器");
        discoveryManager.setPrinterDiscoveryListener(wrappedListener);
        discoveryManager.startDiscovery();
    }
    
    /**
     * 创建并测试多个端口的打印机连接
     * @param printerName 打印机名称
     * @param address 打印机地址
     * @param ports 要测试的端口数组
     * @param listener 发现监听器
     */
    private void createAndTestPrinterWithPorts(String printerName, String address, int[] ports, PrinterDiscoveryManager.PrinterDiscoveryListener listener) {
        if (ports == null || ports.length == 0) {
            Log.e(TAG, "没有指定要测试的端口");
            return;
        }
        
        // 对于第一个端口，使用普通名称
        PrinterInfo firstPrinter = PrinterInfo.fromWiFi(
                printerName + " (打印机热点)", 
                address, 
                ports[0]
        );
        
        // 异步测试第一个端口
        testPrinterConnectionAsync(firstPrinter, success -> {
            if (success) {
                Log.d(TAG, "成功连接到打印机热点: " + address + ":" + ports[0]);
                listener.onPrinterFound(firstPrinter);
                return;
            }
            
            // 如果第一个端口失败，尝试其他端口
            if (ports.length > 1) {
                Log.d(TAG, "尝试连接备用端口");
                for (int i = 1; i < ports.length; i++) {
                    final int port = ports[i];
                    PrinterInfo altPrinter = PrinterInfo.fromWiFi(
                            printerName + " (打印机热点 端口:" + port + ")", 
                            address, 
                            port
                    );
                    
                    // 为每个端口创建单独的测试连接
                    testPrinterConnectionAsync(altPrinter, altSuccess -> {
                        if (altSuccess) {
                            Log.d(TAG, "成功连接到打印机热点备用端口: " + address + ":" + port);
                            listener.onPrinterFound(altPrinter);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * 异步测试打印机连接
     * @param printer 打印机信息
     * @param callback 结果回调
     */
    private void testPrinterConnectionAsync(PrinterInfo printer, Consumer<Boolean> callback) {
        new Thread(() -> {
            boolean result = testPrinterConnection(printer);
            mainHandler.post(() -> callback.accept(result));
        }).start();
    }
    
    /**
     * 测试打印机连接是否可用
     * @param printer 打印机信息
     * @return 是否连接成功
     */
    private boolean testPrinterConnection(PrinterInfo printer) {
        Log.d(TAG, "测试打印机连接: " + printer.getAddress() + ":" + printer.getPort());
        
        boolean success = false;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(printer.getAddress(), printer.getPort()), 2000); // 2秒超时
            socket.close();
            success = true;
            Log.d(TAG, "成功连接到打印机: " + printer.getAddress() + ":" + printer.getPort());
        } catch (Exception e) {
            // 连接失败，记录日志但不抛出异常
            Log.d(TAG, "连接打印机失败: " + printer.getAddress() + ":" + printer.getPort() + " - " + e.getMessage());
        }
        
        return success;
    }

    /**
     * 检查SSID是否可能是打印机热点
     * @param ssid WiFi的SSID
     * @return 是否可能是打印机热点
     */
    private boolean isPrinterHotspotSSID(String ssid) {
        if (ssid == null || ssid.isEmpty()) {
            return false;
        }
        
        // 转为小写进行比较
        String lowerSsid = ssid.toLowerCase();
        
        // 检查SSID是否包含打印机相关关键词
        return lowerSsid.contains("printer") || 
               lowerSsid.contains("print") ||
               lowerSsid.contains("hp") ||
               lowerSsid.contains("epson") ||
               lowerSsid.contains("canon") ||
               lowerSsid.contains("brother") ||
               lowerSsid.contains("zebra") ||
               lowerSsid.contains("thermal") ||
               lowerSsid.contains("receipt") ||
               lowerSsid.contains("pos") ||
               lowerSsid.contains("wifi-direct");
    }

    /**
     * 检查常见的打印机IP地址
     * @param listener 发现监听器
     */
    private void checkCommonPrinterAddresses(PrinterDiscoveryManager.PrinterDiscoveryListener listener) {
        // 热点模式下，不再枚举常见IP地址，而是仅使用网关地址
        Log.d(TAG, "热点模式下使用网关地址连接打印机，不检查其他IP地址");
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
     * 停止发现打印机
     */
    public void stopDiscovery() {
        Log.d(TAG, "停止搜索WiFi打印机");
        discoveryManager.stopDiscovery();
    }
    
    /**
     * 检查打印服务是否启用
     * @return 服务是否启用
     */
    @Override
    public boolean isPrinterServiceEnabled() {
        // 检查网络连接状态
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                
                boolean hasWifi = capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                Log.d(TAG, "WiFi网络状态: " + (hasWifi ? "可用" : "不可用"));
                return hasWifi;
            }
        } catch (Exception e) {
            Log.e(TAG, "检查WiFi服务状态时出错", e);
        }
        
        Log.d(TAG, "无法确定WiFi状态，默认返回true");
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
    
    /**
     * 检查是否有WiFi访问权限
     * @return 是否有权限
     */
    private boolean checkWifiPermission() {
        // ACCESS_WIFI_STATE权限检查
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "缺少ACCESS_WIFI_STATE权限");
            
            // 如果context是Activity，尝试请求权限
            if (context instanceof Activity) {
                Log.d(TAG, "尝试请求ACCESS_WIFI_STATE权限");
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                        REQUEST_WIFI_PERMISSION);
            }
            return false;
        }
        
        // 检查Android 10及以上版本可能需要的位置权限(获取SSID需要)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "缺少ACCESS_FINE_LOCATION权限，在Android 10及以上版本获取SSID需要此权限");
                
                // 如果context是Activity，尝试请求权限
                if (context instanceof Activity) {
                    Log.d(TAG, "尝试请求ACCESS_FINE_LOCATION权限");
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }
                return false;
            }
        }
        
        Log.d(TAG, "已获取WiFi访问所需权限");
        return true;
    }
}
