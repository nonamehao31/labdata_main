package com.example.labdata_main.printer;

import android.bluetooth.BluetoothDevice;

/**
 * 打印机信息类，用于存储打印机连接信息
 */
public class PrinterInfo {
    public static final int TYPE_BLUETOOTH = 1;
    public static final int TYPE_WIFI = 2;
    
    private int type;
    private String name;
    private String address;
    private int port = 9100; // 大多数网络打印机默认端口
    private BluetoothDevice bluetoothDevice; // 可选，用于蓝牙打印机
    
    /**
     * 创建蓝牙打印机信息
     * @param device 蓝牙设备
     * @return 打印机信息
     */
    public static PrinterInfo fromBluetoothDevice(BluetoothDevice device) {
        PrinterInfo info = new PrinterInfo();
        info.type = TYPE_BLUETOOTH;
        info.name = device.getName();
        info.address = device.getAddress();
        info.bluetoothDevice = device;
        return info;
    }
    
    /**
     * 创建蓝牙打印机信息（不需要蓝牙设备对象）
     * @param name 打印机名称
     * @param address 蓝牙地址
     * @return 打印机信息
     */
    public static PrinterInfo fromBluetooth(String name, String address) {
        PrinterInfo info = new PrinterInfo();
        info.type = TYPE_BLUETOOTH;
        info.name = name;
        info.address = address;
        return info;
    }
    
    /**
     * 创建WiFi打印机信息
     * @param name 打印机名称
     * @param ipAddress IP地址
     * @param port 端口号
     * @return 打印机信息
     */
    public static PrinterInfo fromWiFi(String name, String ipAddress, int port) {
        PrinterInfo info = new PrinterInfo();
        info.type = TYPE_WIFI;
        info.name = name;
        info.address = ipAddress;
        info.port = port;
        return info;
    }
    
    // Getters
    public int getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
    
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
    
    @Override
    public String toString() {
        return name + " (" + (type == TYPE_BLUETOOTH ? "蓝牙" : "WiFi") + ")";
    }
}
