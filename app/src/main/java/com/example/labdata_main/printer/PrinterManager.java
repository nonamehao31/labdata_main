package com.example.labdata_main.printer;

import android.graphics.Bitmap;

/**
 * 打印机管理器通用接口，支持不同类型的打印机连接
 */
public interface PrinterManager {
    
    /**
     * 打印状态回调接口
     */
    interface PrintCallback {
        void onPrintStart();
        void onPrintSuccess();
        void onPrintError(String error);
    }
    
    /**
     * 检查是否启用了打印服务
     * @return 如果服务启用返回true，否则返回false
     */
    boolean isPrinterServiceEnabled();
    
    /**
     * 连接到打印机
     * @param printerInfo 打印机信息
     * @param callback 打印回调
     */
    void connectToPrinter(PrinterInfo printerInfo, PrintCallback callback);
    
    /**
     * 打印二维码
     * @param qrCode 二维码位图
     * @param callback 打印回调
     */
    void printQRCode(Bitmap qrCode, PrintCallback callback);
    
    /**
     * 断开打印机连接
     */
    void disconnect();
}
