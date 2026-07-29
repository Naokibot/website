package dev.webdefense.core;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ViolationTracker {
    private final Deque<Instant> violations=new ArrayDeque<>();
    private Instant blockedUntil=Instant.EPOCH,lastSeen=Instant.EPOCH;
    public synchronized boolean isBlocked(Instant now){ lastSeen=now; return now.isBefore(blockedUntil); }
    public synchronized Instant blockedUntil(){ return blockedUntil; }
    public synchronized boolean record(Instant now,int threshold,Duration window,Duration blockDuration){
        lastSeen=now; Instant cutoff=now.minus(window);
        while(!violations.isEmpty()&&violations.peekFirst().isBefore(cutoff)) violations.removeFirst();
        violations.addLast(now);
        if(violations.size()>=threshold){ blockedUntil=now.plus(blockDuration); violations.clear(); return true; }
        return false;
    }
    public synchronized Instant lastSeen(){ return lastSeen; }
}
