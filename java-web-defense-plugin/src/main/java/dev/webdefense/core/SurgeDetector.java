package dev.webdefense.core;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

public final class SurgeDetector {
    private final int bucketSeconds; private final long minimumRps; private final double multiplier,alpha;
    private final Duration protectionDuration; private final LongAdder currentCount=new LongAdder();
    private volatile long bucketStartEpochSecond,lastCompletedRps; private volatile double ewmaRps;
    private volatile Instant protectionUntil=Instant.EPOCH;
    public SurgeDetector(int bucketSeconds,long minimumRps,double multiplier,double alpha,Duration protectionDuration){
        this.bucketSeconds=bucketSeconds; this.minimumRps=minimumRps; this.multiplier=multiplier; this.alpha=alpha;
        this.protectionDuration=protectionDuration; this.bucketStartEpochSecond=Instant.now().getEpochSecond();
    }
    public void record(Instant now){ rotateIfNeeded(now); currentCount.increment(); }
    public boolean isProtectionMode(Instant now){ rotateIfNeeded(now); return now.isBefore(protectionUntil); }
    public Snapshot snapshot(Instant now){ rotateIfNeeded(now); return new Snapshot(lastCompletedRps,ewmaRps,protectionUntil,now.isBefore(protectionUntil)); }
    private synchronized void rotateIfNeeded(Instant now){
        long currentSecond=now.getEpochSecond(), elapsed=currentSecond-bucketStartEpochSecond;
        if(elapsed<bucketSeconds) return;
        long count=currentCount.sumThenReset(); double rps=count/(double)Math.max(bucketSeconds,elapsed); lastCompletedRps=Math.round(rps);
        double previous=ewmaRps;
        if(previous<=0) ewmaRps=rps;
        else { double threshold=Math.max(minimumRps,previous*multiplier); if(rps>=threshold) protectionUntil=now.plus(protectionDuration); ewmaRps=alpha*rps+(1-alpha)*previous; }
        bucketStartEpochSecond=currentSecond;
    }
    public record Snapshot(long lastRps,double ewmaRps,Instant protectionUntil,boolean protectionMode){}
}
