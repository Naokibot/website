package dev.webdefense.core;

import java.time.Instant;
import java.util.Map;

public record RequestContext(String requestId, Instant timestamp, String clientIp, String method,
                             String path, String userAgent, long contentLength,
                             String remotePeerIp, Map<String,String> headers) {
    public RequestContext { headers = Map.copyOf(headers); }
}
