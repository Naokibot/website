package dev.webdefense.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AsyncJsonLogger implements AutoCloseable {
 private final BlockingQueue<String> queue; private final DefenseMetrics metrics; private final boolean includeAllowed,hashIp; private final String hashSalt; private final AtomicBoolean running=new AtomicBoolean(true); private final Thread worker;
 public AsyncJsonLogger(Path path,int capacity,DefenseMetrics metrics,boolean includeAllowed,boolean hashIp,String hashSalt)throws IOException{
  this.queue=new ArrayBlockingQueue<>(capacity);this.metrics=metrics;this.includeAllowed=includeAllowed;this.hashIp=hashIp;this.hashSalt=hashSalt;
  Path parent=path.toAbsolutePath().getParent();if(parent!=null)Files.createDirectories(parent);
  worker=Thread.ofPlatform().name("web-defense-log-writer").daemon(true).start(()->runWriter(path));
 }
 public void log(RequestContext r,Decision d,long latencyMicros){if(d.allowed()&&!includeAllowed)return;Map<String,Object> e=new LinkedHashMap<>();e.put("timestamp",Instant.now());e.put("requestId",safe(r.requestId(),80));e.put("clientIp",hashIp?hash(r.clientIp()):safe(r.clientIp(),80));e.put("remotePeerIp",hashIp?hash(r.remotePeerIp()):safe(r.remotePeerIp(),80));e.put("method",safe(r.method(),16));e.put("path",safe(r.path(),2048));e.put("userAgent",safe(r.userAgent(),512));e.put("contentLength",r.contentLength());e.put("allowed",d.allowed());e.put("status",d.statusCode());e.put("reason",d.reason());e.put("retryAfterSeconds",d.retryAfterSeconds());e.put("remaining",d.remaining());e.put("resetAt",d.resetAt());e.put("protectionMode",d.protectionMode());e.put("latencyMicros",latencyMicros);e.put("details",d.details());if(!queue.offer(JsonUtil.toJson(e)))metrics.logDropped();}
 private void runWriter(Path path){try(BufferedWriter out=Files.newBufferedWriter(path,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND)){while(running.get()||!queue.isEmpty()){String line=queue.poll(500,TimeUnit.MILLISECONDS);if(line!=null){out.write(line);out.newLine();}if(line==null||queue.isEmpty())out.flush();}}catch(InterruptedException e){Thread.currentThread().interrupt();}catch(IOException e){System.err.println("[web-defense] Log writer stopped: "+e.getMessage());}}
 private String hash(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((hashSalt+"|"+v).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){return "hash-error";}}
 private static String safe(String v,int max){if(v==null)return "";String c=v.replaceAll("[\\p{Cntrl}&&[^\\t]]","?");return c.length()<=max?c:c.substring(0,max);}
 public void close(){running.set(false);worker.interrupt();try{worker.join(2000);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
}
