package com.example.monitor.common;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class TokenUtil {
  private static final String SECRET = "change-me";
  public static String issue(String user) {
    String payload = user + ":" + Instant.now().getEpochSecond();
    String sign = Integer.toHexString((payload + SECRET).hashCode());
    return Base64.getUrlEncoder().encodeToString((payload+":"+sign).getBytes(StandardCharsets.UTF_8));
  }
  public static boolean validate(String token) {
    try {
      String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
      String[] parts = raw.split(":");
      if (parts.length < 3) return false;
      String payload = parts[0] + ":" + parts[1];
      String sign = Integer.toHexString((payload + SECRET).hashCode());
      return sign.equals(parts[2]);
    } catch (Exception e) { return false; }
  }
}