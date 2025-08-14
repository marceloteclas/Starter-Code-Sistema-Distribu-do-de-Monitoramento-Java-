package com.example.monitor.common;

import java.util.concurrent.atomic.AtomicLong;

public class LamportClock {
  private final AtomicLong clock = new AtomicLong(0);
  public long tick() { return clock.incrementAndGet(); }
  public long onReceive(long incoming) { return clock.updateAndGet(local -> Math.max(local, incoming) + 1); }
  public long now() { return clock.get(); }
}