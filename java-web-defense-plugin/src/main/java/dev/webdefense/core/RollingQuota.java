package dev.webdefense.core;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public final class RollingQuota {
    private final Deque<Instant> timestamps=new ArrayDeque<>();
    private Instant cooldownUntil=Instant.EPOCH,lastSeen=Instant.EPOCH;
    public synchronized Result consume(Instant now,int maxRequests,Duration window,Duration cooldown){
        lastSeen=now;
        if(now.isBefore(cooldownUntil)) return new Result(false,0,cooldownUntil,Math.max(1,Duration.between(now,cooldownUntil).toSeconds()));
        Instant cutoff=now.minus(window);
        while(!timestamps.isEmpty()&&timestamps.peekFirst().isBefore(cutoff)) timestamps.removeFirst();
        if(timestamps.size()>=maxRequests){ cooldownUntil=now.plus(cooldown); return new Result(false,0,cooldownUntil,cooldown.toSeconds()); }
        timestamps.addLast(now);
        long remaining=Math.max(0,maxRequests-timestamps.size());
        Instant resetAt=timestamps.peekFirst().plus(window);
        return new Result(true,remaining,resetAt,0);
    }
    public synchronized Instant lastSeen(){ return lastSeen; }
    public record Result(boolean allowed,long remaining,Instant resetAt,long retryAfterSeconds){}
}
