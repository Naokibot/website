package dev.webdefense.core;

public final class TokenBucket {
    private double capacity, refillPerNano, tokens;
    private long lastRefillNanos;
    public TokenBucket(double capacity,double refillPerSecond,long nowNanos){ reconfigure(capacity,refillPerSecond,nowNanos); tokens=capacity; }
    public synchronized Result tryConsume(double amount,double newCapacity,double newRefillPerSecond,long nowNanos){
        if(capacity!=newCapacity||refillPerNano!=newRefillPerSecond/1_000_000_000d){
            refill(nowNanos); double fraction=capacity<=0?0:tokens/capacity; capacity=newCapacity;
            refillPerNano=newRefillPerSecond/1_000_000_000d; tokens=Math.min(capacity,Math.max(0,fraction*capacity));
        } else refill(nowNanos);
        if(tokens>=amount){ tokens-=amount; return new Result(true,(long)Math.floor(tokens),0); }
        double missing=amount-tokens;
        long retryNanos=refillPerNano<=0?Long.MAX_VALUE:(long)Math.ceil(missing/refillPerNano);
        return new Result(false,(long)Math.floor(tokens),retryNanos==Long.MAX_VALUE?86400:Math.max(1,retryNanos/1_000_000_000L));
    }
    private void refill(long now){ long elapsed=Math.max(0,now-lastRefillNanos); tokens=Math.min(capacity,tokens+elapsed*refillPerNano); lastRefillNanos=now; }
    private void reconfigure(double capacity,double refill,long now){ this.capacity=capacity; this.refillPerNano=refill/1_000_000_000d; this.lastRefillNanos=now; }
    public record Result(boolean allowed,long remaining,long retryAfterSeconds){}
}
