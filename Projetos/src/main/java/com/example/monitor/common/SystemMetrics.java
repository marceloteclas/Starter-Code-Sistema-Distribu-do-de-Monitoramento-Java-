package com.example.monitor.common;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class SystemMetrics {
    private static final long START = System.currentTimeMillis();
    
    public static double cpuLoad() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) os).getSystemCpuLoad();
        }
        return 0.0;
    }
    
    public static double memUsage() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOs = (com.sun.management.OperatingSystemMXBean) os;
            double used = sunOs.getTotalMemorySize() - sunOs.getFreeMemorySize();
            return sunOs.getTotalMemorySize() == 0 ? 0 : used / sunOs.getTotalMemorySize();
        }
        return 0.0;
    }
    
    public static long uptimeSeconds() { 
        return (System.currentTimeMillis() - START) / 1000; 
    }
}