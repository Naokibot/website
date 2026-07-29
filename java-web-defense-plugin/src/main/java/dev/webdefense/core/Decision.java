package dev.webdefense.core;

import java.time.Instant;
import java.util.Map;

public record Decision(boolean allowed, int statusCode, String reason, long retryAfterSeconds,
                       long remaining, Instant resetAt, boolean protectionMode,
                       Map<String,Object> details) {
    public static Decision allow(long remaining, Instant resetAt, boolean protectionMode, Map<String,Object> details) {
        return new Decision(true, 200, "ALLOWED", 0, remaining, resetAt, protectionMode, Map.copyOf(details));
    }
    public static Decision deny(int statusCode, String reason, long retryAfterSeconds, long remaining,
                                Instant resetAt, boolean protectionMode, Map<String,Object> details) {
        return new Decision(false, statusCode, reason, Math.max(1,retryAfterSeconds), Math.max(0,remaining),
                resetAt, protectionMode, Map.copyOf(details));
    }
}
