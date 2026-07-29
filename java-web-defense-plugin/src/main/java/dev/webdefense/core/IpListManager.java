package dev.webdefense.core;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class IpListManager {
 private final Path allowPath,denyPath,trustedPath; private final Duration reloadEvery;
 private final ReentrantReadWriteLock lock=new ReentrantReadWriteLock(); private volatile Instant nextReload=Instant.EPOCH;
 private List<CidrBlock> allow=List.of(),deny=List.of(),trusted=List.of();
 public IpListManager(Path a,Path d,Path t,Duration r){allowPath=a;denyPath=d;trustedPath=t;reloadEvery=r;reloadNow();}
 public boolean isAllowed(String ip){reloadIfDue();lock.readLock().lock();try{return allow.stream().anyMatch(c->c.contains(ip));}finally{lock.readLock().unlock();}}
 public boolean isDenied(String ip){reloadIfDue();lock.readLock().lock();try{return deny.stream().anyMatch(c->c.contains(ip));}finally{lock.readLock().unlock();}}
 public boolean isTrustedProxy(String ip){reloadIfDue();lock.readLock().lock();try{return trusted.stream().anyMatch(c->c.contains(ip));}finally{lock.readLock().unlock();}}
 public void reloadIfDue(){if(Instant.now().isAfter(nextReload))reloadNow();}
 public void reloadNow(){lock.writeLock().lock();try{allow=read(allowPath);deny=read(denyPath);trusted=read(trustedPath);nextReload=Instant.now().plus(reloadEvery);}finally{lock.writeLock().unlock();}}
 private static List<CidrBlock> read(Path path){if(!Files.exists(path))return List.of();List<CidrBlock> out=new ArrayList<>();try{for(String line:Files.readAllLines(path)){String v=line.strip();if(v.isEmpty()||v.startsWith("#"))continue;out.add(CidrBlock.parse(v));}}catch(IOException|IllegalArgumentException e){System.err.println("[web-defense] Could not load "+path+": "+e.getMessage());}return List.copyOf(out);}
}
