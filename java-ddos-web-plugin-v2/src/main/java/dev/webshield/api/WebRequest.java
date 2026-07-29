package dev.webshield.api;
import java.time.Instant;
import java.util.Map;
public record WebRequest(String clientIp,String method,String path,Map<String,String> headers,Instant receivedAt){
 public WebRequest{headers=headers==null?Map.of():Map.copyOf(headers);receivedAt=receivedAt==null?Instant.now():receivedAt;}
}
