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
    // 添加搜索下一种服务类型的延迟
    private static final long SERVICE_TYPE_SEARCH_DELAY = 2000; // 毫秒
    
    private final Context context;
    private final NsdManager nsdManager;
    private final Map<String, PrinterInfo> discoveredPrinters = new HashMap<>();
    private NsdManager.DiscoveryListener discoveryListener;
    private PrinterDiscoveryListener listener;
    private android.os.Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private boolean isDiscoveryActive = false;
    // 添加跟踪当前搜索的服务类型
    private String currentSearchServiceType = null;
    // 添加跟踪已搜索的服务类型计数
    private int searchedServiceTypeCount = 0;
    
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
        searchedServiceTypeCount = 0;
        
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
                Log.d(TAG, "执行超时停止搜索，当前搜索的服务类型: " + currentSearchServiceType);
                stopDiscovery();
                
                // 通知搜索完成
                if (listener != null) {
                    Log.d(TAG, "通知UI搜索已完成（超时），总共发现打印机数量: " + discoveredPrinters.size());
                    listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                }
            }
        };
        
        // 设置超时
        timeoutHandler.postDelayed(timeoutRunnable, DISCOVERY_TIMEOUT);
        
        // 开始搜索IPP协议打印机
        startSearchForServiceType(SERVICE_TYPE_IPP);
    }
    
    /**
     * 开始搜索特定服务类型的打印机
     * @param serviceType 服务类型
     */
    private void startSearchForServiceType(String serviceType) {
        Log.d(TAG, "开始搜索服务类型: " + serviceType + ", 这是第 " + (searchedServiceTypeCount + 1) + " 种尝试的服务类型");
        
        currentSearchServiceType = serviceType;
        searchedServiceTypeCount++;
        
        // 创建发现监听器
        createDiscoveryListener();
        
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            Log.d(TAG, "已启动对 " + serviceType + " 服务类型的搜索");
        } catch (Exception e) {
            Log.e(TAG, "搜索 " + serviceType + " 服务类型时发生异常", e);
            
            // 如果某种服务类型搜索失败，尝试搜索下一种类型
            scheduleNextServiceTypeSearch();
            
            // 如果所有类型都已尝试，通知错误
            if (searchedServiceTypeCount >= 3) {
                if (listener != null && isDiscoveryActive) {
                    Log.e(TAG, "所有服务类型搜索均失败");
                    listener.onDiscoveryError("所有打印机服务类型搜索均失败: " + e.getMessage());
                    stopDiscovery();
                }
            }
        }
    }
    
    /**
     * 安排搜索下一种服务类型
     */
    private void scheduleNextServiceTypeSearch() {
        if (!isDiscoveryActive) {
            Log.d(TAG, "搜索已停止，不再安排下一种服务类型搜索");
            return;
        }
        
        // 确定下一种要搜索的服务类型
        String nextServiceType = null;
        if (SERVICE_TYPE_IPP.equals(currentSearchServiceType)) {
            nextServiceType = SERVICE_TYPE_PDL;
        } else if (SERVICE_TYPE_PDL.equals(currentSearchServiceType)) {
            nextServiceType = SERVICE_TYPE_PRINTER;
        }
        
        if (nextServiceType != null) {
            final String serviceType = nextServiceType;
            Log.d(TAG, "安排 " + SERVICE_TYPE_SEARCH_DELAY + "ms 后搜索下一种服务类型: " + serviceType);
            
            timeoutHandler.postDelayed(() -> {
                if (isDiscoveryActive) {
                    // 先停止当前搜索
                    try {
                        if (discoveryListener != null) {
                            Log.d(TAG, "停止当前搜索以开始下一种服务类型搜索");
                            nsdManager.stopServiceDiscovery(discoveryListener);
                            discoveryListener = null;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "停止当前搜索时出错", e);
                    }
                    
                    // 开始下一种类型搜索
                    startSearchForServiceType(serviceType);
                } else {
                    Log.d(TAG, "搜索已停止，取消搜索下一种服务类型");
                }
            }, SERVICE_TYPE_SEARCH_DELAY);
        } else {
            Log.d(TAG, "已尝试所有服务类型，不再安排新的搜索");
        }
    }
    
    /**
     * 创建服务发现监听器
     */
    private void createDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "服务发现已开始: " + serviceType);
            }
            
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "发现服务: " + serviceInfo.getServiceName() + ", 服务类型: " + serviceInfo.getServiceType());
                
                // 解析服务信息
                nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "解析服务失败: " + serviceInfo.getServiceName() + ", 错误码: " + errorCode);
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
                        
                        Log.d(TAG, "找到打印机: 名称=" + name + ", 地址=" + host.getHostAddress() + ", 端口=" + port);
                        
                        // 创建打印机信息对象
                        PrinterInfo printerInfo = PrinterInfo.fromWiFi(
                                name,
                                host.getHostAddress(),
                                port
                        );
                        
                        // 存储发现的打印机
                        String key = host.getHostAddress() + ":" + port;
                        if (!discoveredPrinters.containsKey(key)) {
                            Log.d(TAG, "添加新发现的打印机到列表中: " + key);
                            discoveredPrinters.put(key, printerInfo);
                            
                            // 通知发现了新的打印机
                            if (listener != null) {
                                listener.onPrinterFound(printerInfo);
                                
                                // 如果找到至少一台打印机，并且搜索仍在进行，立即通知UI可以显示结果
                                if (discoveredPrinters.size() == 1 && isDiscoveryActive) {
                                    Log.d(TAG, "已找到第一台打印机，通知UI可以停止显示加载状态");
                                    // 注意这里不停止搜索，但通知UI可以停止显示加载状态
                                    listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                                }
                            }
                        } else {
                            Log.d(TAG, "打印机已在列表中，忽略: " + key);
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
                    
                    // 如果还有其他服务类型需要搜索，开始下一个搜索
                    if (isDiscoveryActive) {
                        scheduleNextServiceTypeSearch();
                    } else {
                        // 通知搜索完成
                        if (listener != null) {
                            Log.d(TAG, "搜索全部完成，通知UI，找到 " + discoveredPrinters.size() + " 台打印机");
                            listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
                        }
                    }
                }
            }
            
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "启动服务发现失败: " + serviceType + ", 错误码: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("启动服务发现失败: " + errorCode);
                }
                
                // 尝试搜索下一种类型
                scheduleNextServiceTypeSearch();
                
                // 重置状态
                discoveryListener = null;
            }
            
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "停止服务发现失败: " + serviceType + ", 错误码: " + errorCode);
                
                if (listener != null) {
                    listener.onDiscoveryError("停止服务发现失败: " + errorCode);
                }
                
                // 重置状态
                discoveryListener = null;
                
                // 尝试搜索下一种类型
                scheduleNextServiceTypeSearch();
            }
        };
    }
    
    /**
     * 停止发现打印机
     */
    public void stopDiscovery() {
        Log.d(TAG, "停止搜索打印机，当前状态: isDiscoveryActive=" + isDiscoveryActive + 
                  ", discoveryListener=" + (discoveryListener != null) + 
                  ", 当前服务类型=" + currentSearchServiceType);
        
        // 取消超时任务
        if (timeoutHandler != null && timeoutRunnable != null) {
            Log.d(TAG, "取消超时任务");
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        // 移除所有延迟执行的任务
        if (timeoutHandler != null) {
            Log.d(TAG, "移除所有延迟任务");
            timeoutHandler.removeCallbacksAndMessages(null);
        }
        
        boolean wasActive = isDiscoveryActive;
        isDiscoveryActive = false;
        
        if (discoveryListener != null) {
            Log.d(TAG, "停止NSD服务发现");
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } catch (Exception e) {
                Log.e(TAG, "停止服务发现时发生错误", e);
            }
            
            discoveryListener = null;
        }
        
        // 无论如何，确保通知UI搜索已结束
        if (wasActive && listener != null) {
            Log.d(TAG, "确认通知UI搜索已结束，找到 " + discoveredPrinters.size() + " 台打印机");
            listener.onPrinterDiscoveryFinished(new ArrayList<>(discoveredPrinters.values()));
        }
        
        // 清除当前状态
        currentSearchServiceType = null;
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
