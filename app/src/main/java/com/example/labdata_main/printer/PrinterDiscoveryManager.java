package com.example.labdata_main.printer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机发现管理器，使用Android NSD服务发现网络上的打印机
 */
public class PrinterDiscoveryManager {
    private static final String TAG = "PrinterDiscoveryManager";
    
    // 打印服务类型
    private static final String SERVICE_TYPE_IPP = "_ipp._tcp.";       // IPP打印协议
    private static final String SERVICE_TYPE_PDL = "_pdl-datastream._tcp."; // PDL打印协议
    private static final String SERVICE_TYPE_PRINTER = "_printer._tcp."; // 通用打印机服务
    
    // 添加超时时间常量，10秒
    private static final long DISCOVERY_TIMEOUT = 10000; // 毫秒
    
    private final Context context;
    private final NsdManager nsdManager;
    private final Map<String, PrinterInfo> discoveredPrinters = new HashMap<>();
    private NsdManager.DiscoveryListener discoveryListener;
    private PrinterDiscoveryListener listener;
    private android.os.Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private boolean isDiscoveryActive = false;
    
    /**
     * 打印机发现回调接口
     */
    public interface PrinterDiscoveryListener {
        void onPrinterDiscoveryStarted();
        void onPrinterFound(PrinterInfo printer);
        void onPrinterDiscoveryFinished(List<PrinterInfo> printers);
        void onDiscoveryError(String errorMessage);
    }
    
    /**
     * 构造函数
     * @param context 上下文
     */
    public PrinterDiscoveryManager(Context context) {
        this.context = context;
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }
    
    /**
     * 设置打印机发现监听器
     * @param listener 监听器
     */
    public void setPrinterDiscoveryListener(PrinterDiscoveryListener listener) {
        this.listener = listener;
    }
    
    /**
     * 开始发现打印机
     */
    public void startDiscovery() {
        Log.d(TAG, "开始搜索打印机");
        
        // 如果已经在搜索，先停止当前搜索
        if (isDiscoveryActive || discoveryListener != null) {
            Log.d(TAG, "已有搜索进行中，先停止");
            stopDiscovery();
        }
        
        isDiscoveryActive = true;
        discoveredPrinters.clear();
        
        if (listener != null) {
            listener.onPrinterDiscoveryStarted();
        }
        
        // 设置超时处理
        if (timeoutHandler == null) {
            timeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        
        // 创建并执行超时任务
        timeoutRunnable = () -> {
            Log.d(TAG, "打印机搜索超时，停止搜索");
            if (isDiscoveryActive) {
                Log.d(TAG, "执行超时停止搜索");
                stopDiscovery();
                
                // 通知搜索完成
                if (listener != null) {
                    Log.d(TAG, "通知UI搜索已完成（超时）");
                    listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                }
            }
        };
        
        // 设置超时
        timeoutHandler.postDelayed(timeoutRunnable, DISCOVERY_TIMEOUT);
        
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "服务发现已开始: " + serviceType);
            }
            
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "发现服务: " + serviceInfo.getServiceName());
                
                // 解析服务信息
                nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "解析服务失败: " + errorCode);
                    }
                    
                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "已解析服务: " + serviceInfo.getServiceName());
                        
                        if (!isDiscoveryActive) {
                            Log.d(TAG, "搜索已停止，忽略新找到的打印机");
                            return;
                        }
                        
                        InetAddress host = serviceInfo.getHost();
                        int port = serviceInfo.getPort();
                        String name = serviceInfo.getServiceName();
                        
                        // 创建打印机信息对象
                        PrinterInfo printerInfo = PrinterInfo.fromWiFi(
                                name,
                                host.getHostAddress(),
                                port
                        );
                        
                        // 存储发现的打印机
                        String key = host.getHostAddress() + ":" + port;
                        if (!discoveredPrinters.containsKey(key)) {
                            discoveredPrinters.put(key, printerInfo);
                            
                            // 通知发现了新的打印机
                            if (listener != null) {
                                listener.onPrinterFound(printerInfo);
                                
                                // 如果找到至少一台打印机，并且搜索仍在进行，立即通知UI可以显示结果
                                if (discoveredPrinters.size() == 1 && isDiscoveryActive) {
                                    // 注意这里不停止搜索，但通知UI可以停止显示加载状态
                                    listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                                }
                            }
                        }
                    }
                });
            }
            
            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "服务丢失: " + serviceInfo.getServiceName());
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "服务发现已停止: " + serviceType);
                
                // 只有当发现监听器与当前监听器相同时才处理
                // 防止旧的停止回调误触发
                if (discoveryListener != null) {
                    discoveryListener = null;
                    
                    // 通知搜索完成
                    if (listener != null && isDiscoveryActive) {
                        isDiscoveryActive = false;
                        listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                    }
                }
            }
            
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "启动服务发现失败: " + serviceType + ", 错误码: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("启动服务发现失败: " + errorCode);
                }
                
                // 重置状态
                isDiscoveryActive = false;
                discoveryListener = null;
            }
            
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "停止服务发现失败: " + serviceType + ", 错误码: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("停止服务发现失败: " + errorCode);
                }
                
                // 重置状态
                isDiscoveryActive = false;
                discoveryListener = null;
            }
        };
        
        try {
            nsdManager.discoverServices(SERVICE_TYPE_IPP, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        } catch (Exception e) {
            Log.e(TAG, "启动服务发现异常", e);
            if (listener != null) {
                listener.onDiscoveryError("启动服务发现异常: " + e.getMessage());
            }
            isDiscoveryActive = false;
            discoveryListener = null;
        }
    }
    
    /**
     * 停止发现打印机
     */
    public void stopDiscovery() {
        Log.d(TAG, "停止搜索打印机");
        
        // 取消超时任务
        if (timeoutHandler != null && timeoutRunnable != null) {
            Log.d(TAG, "取消超时任务");
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        if (discoveryListener != null) {
            Log.d(TAG, "停止NSD服务发现");
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } catch (Exception e) {
                Log.e(TAG, "停止服务发现时发生错误", e);
            }
            
            // 即使stopServiceDiscovery失败，也需要重置状态
            // 因为onDiscoveryStopped回调可能不会被调用
            discoveryListener = null;
        }
        
        // 无论如何，设置标志位为false
        isDiscoveryActive = false;
    }
    
    /**
     * 获取所有发现的打印机
     * @return 打印机列表
     */
    public List<PrinterInfo> getDiscoveredPrinters() {
        return new ArrayList<>(discoveredPrinters.values());
    }
    
    /**
     * 使用IP和端口直接添加打印机（手动添加）
     * @param name 打印机名称
     * @param ipAddress IP地址
     * @param port 端口号
     */
    public void addPrinterManually(String name, String ipAddress, int port) {
        PrinterInfo printerInfo = PrinterInfo.fromWiFi(name, ipAddress, port);
        String key = ipAddress + ":" + port;
        
        discoveredPrinters.put(key, printerInfo);
        
        if (listener != null) {
            listener.onPrinterFound(printerInfo);
        }
    }
}
