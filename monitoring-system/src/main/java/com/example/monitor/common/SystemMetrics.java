package com.example.monitor.common;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class SystemMetrics {
  private static final long START = System.currentTimeMillis();
  public static double cpuLoad() {
    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    double load = os.getSystemCpuLoad();
    return Double.isNaN(load) ? 0.0 : Math.max(0.0, Math.min(1.0, load));
  }
  public static double memUsage() {
    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    double used = (double)(os.getTotalMemorySize() - os.getFreeMemorySize());
    return os.getTotalMemorySize() == 0 ? 0 : used / os.getTotalMemorySize();
  }
  public static long uptimeSec() { return (System.currentTimeMillis() - START) / 1000; }
}