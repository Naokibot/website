package dev.webshield.core;
import dev.webshield.api.*;import java.nio.file.*;import java.time.Instant;import java.util.concurrent.*;
final class JsonAuditLogger implements AutoCloseable{
 private final BlockingQueue<String>q=new ArrayBlockingQueue<>(10000);private final Thread worker;private final Path file;private volatile boolean open=true;
 JsonAuditLogger(Path file){this.file=file;worker=Thread.ofVirtual().name("webshield-log").start(this::run);}
 void log(WebRequest r,Decision d){q.offer("{\"time\":\""+Instant.now()+"\",\"ip\":\""+s(r.clientIp())+"\",\"method\":\""+s(r.method())+"\",\"path\":\""+s(r.path())+"\",\"allowed\":"+d.allowed()+",\"status\":"+d.status()+",\"reason\":\""+s(d.reason())+"\",\"surge\":"+d.surgeMode()+"}");}
 private void run(){try{Files.createDirectories(file.toAbsolutePath().getParent());while(open||!q.isEmpty()){String v=q.poll(200,TimeUnit.MILLISECONDS);if(v!=null)Files.writeString(file,v+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);}}catch(Exception ignored){}}
 private static String s(String x){return x==null?"":x.replace("\\","\\\\").replace("\"","\\\"").replace("\r"," ").replace("\n"," ");}
 public void close(){open=false;try{worker.join(1500);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
}
