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
    
    private final Context context;
    private final NsdManager nsdManager;
    private final Map<String, PrinterInfo> discoveredPrinters = new HashMap<>();
    private NsdManager.DiscoveryListener discoveryListener;
    private PrinterDiscoveryListener listener;
    
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
        if (discoveryListener != null) {
            // 已经在搜索过程中
            return;
        }
        
        discoveredPrinters.clear();
        
        if (listener != null) {
            listener.onPrinterDiscoveryStarted();
        }
        
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "打印机服务发现已启动: " + serviceType);
            }
            
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "找到服务: " + serviceInfo.getServiceName());
                
                // 解析服务详情
                nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "解析服务失败: " + errorCode);
                    }
                    
                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "已解析服务: " + serviceInfo.getServiceName());
                        
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
                
                // 通知搜索完成
                if (listener != null) {
                    listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                }
                
                discoveryListener = null;
            }
            
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "启动服务发现失败: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("启动服务发现失败: " + errorCode);
                }
                
                discoveryListener = null;
            }
            
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "停止服务发现失败: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("停止服务发现失败: " + errorCode);
                }
                
                discoveryListener = null;
            }
        };
        
        // 使用多种打印服务类型搜索
        try {
            nsdManager.discoverServices(SERVICE_TYPE_IPP, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        } catch (Exception e) {
            Log.e(TAG, "IPP服务搜索失败", e);
            // 尝试其他类型
            try {
                nsdManager.discoverServices(SERVICE_TYPE_PDL, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            } catch (Exception e2) {
                Log.e(TAG, "PDL服务搜索失败", e2);
                try {
                    nsdManager.discoverServices(SERVICE_TYPE_PRINTER, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                } catch (Exception e3) {
                    Log.e(TAG, "通用打印机服务搜索失败", e3);
                    if (listener != null) {
                        listener.onDiscoveryError("搜索打印机服务失败");
                    }
                }
            }
        }
    }
    
    /**
     * 停止发现打印机
     */
    public void stopDiscovery() {
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } catch (Exception e) {
                Log.e(TAG, "停止服务发现时发生错误", e);
            }
            discoveryListener = null;
        }
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
