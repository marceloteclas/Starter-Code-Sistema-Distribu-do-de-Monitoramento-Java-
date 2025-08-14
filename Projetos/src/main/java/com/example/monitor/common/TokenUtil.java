package com.example.monitor.common;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class TokenUtil {
    private static final String SECRET = "monitor-secret-key";
    private static final long EXPIRATION_SEC = 3600; // 1 hora
    
    public static String issue(String username) {
        String payload = username + ":" + (Instant.now().getEpochSecond() + EXPIRATION_SEC);
        String signature = Integer.toHexString((payload + SECRET).hashCode());
        return Base64.getUrlEncoder().encodeToString(
            (payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }
    
    public static boolean validate(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) return false;
            
            // Verifica expiração
            long expiration = Long.parseLong(parts[1]);
            if (Instant.now().getEpochSecond() > expiration) {
                return false;
            }
            
            // Verifica assinatura
            String payload = parts[0] + ":" + parts[1];
            String expectedSig = Integer.toHexString((payload + SECRET).hashCode());
            return expectedSig.equals(parts[2]);
        } catch (Exception e) {
            return false;
        }
    }
}