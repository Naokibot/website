package dev.webdefense.core;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DefenseEngine implements AutoCloseable {
 private final DefenseConfig config; private final IpListManager ipLists; private final SurgeDetector surge; private final DefenseMetrics metrics=new DefenseMetrics(); private final AsyncJsonLogger logger; private final ConcurrentHashMap<String,ClientState> clients=new ConcurrentHashMap<>(); private final Set<String> exemptPaths; private volatile long lastCleanupEpochSecond;
 public DefenseEngine(DefenseConfig c)throws Exception{config=c;ipLists=new IpListManager(c.allowlistPath(),c.blacklistPath(),c.trustedProxiesPath(),c.listReload());surge=new SurgeDetector(c.surgeBucketSeconds(),c.surgeMinimumRps(),c.surgeMultiplier(),c.surgeEwmaAlpha(),c.surgeProtection());logger=new AsyncJsonLogger(c.logPath(),c.logQueueCapacity(),metrics,c.logIncludeAllowed(),c.logHashIp(),c.logHashSalt());exemptPaths=Set.copyOf(c.exemptPaths());}
 public Decision evaluate(RequestContext r){long started=System.nanoTime();Instant now=r.timestamp();Decision d;
  if(!config.enabled()){d=Decision.allow(Long.MAX_VALUE,now,false,Map.of("pluginEnabled",false));return finish(r,d,started);}
  String ip=r.clientIp();surge.record(now);boolean protection=surge.isProtectionMode(now);
  if(ipLists.isAllowed(ip)){d=Decision.allow(Long.MAX_VALUE,now.plusSeconds(1),protection,Map.of("ipPolicy","ALLOWLIST"));return finish(r,d,started);}
  if(ipLists.isDenied(ip)){d=Decision.deny(403,"BLACKLIST",config.responseRetryAfterSeconds(),0,now.plusSeconds(config.responseRetryAfterSeconds()),protection,Map.of("ipPolicy","BLACKLIST"));return finish(r,d,started);}
  if(exemptPaths.contains(r.path())){d=Decision.allow(Long.MAX_VALUE,now.plusSeconds(1),protection,Map.of("exemptPath",true));return finish(r,d,started);}
  ClientState s=clients.computeIfAbsent(ip,k->new ClientState(config.rateCapacity(),config.rateRefillPerSecond(),System.nanoTime()));
  if(s.violations.isBlocked(now)){long retry=Math.max(1,Duration.between(now,s.violations.blockedUntil()).toSeconds());d=Decision.deny(429,"TEMPORARY_BLOCK",retry,0,s.violations.blockedUntil(),protection,Map.of("escalated",true));return finish(r,d,started);}
  int capacity=protection?config.surgeStrictCapacity():config.rateCapacity();double refill=protection?config.surgeStrictRefillPerSecond():config.rateRefillPerSecond();
  TokenBucket.Result burst=s.bucket.tryConsume(1,capacity,refill,System.nanoTime());
  if(!burst.allowed()){boolean escalated=s.violations.record(now,config.violationBlockAfter(),config.violationWindow(),config.violationBlock());long retry=escalated?config.violationBlock().toSeconds():Math.max(config.responseRetryAfterSeconds(),burst.retryAfterSeconds());d=Decision.deny(429,protection?"SURGE_RATE_LIMIT":"RATE_LIMIT",retry,burst.remaining(),now.plusSeconds(retry),protection,Map.of("capacity",capacity,"refillPerSecond",refill,"escalated",escalated));return finish(r,d,started);}
  RollingQuota.Result quota=s.quota.consume(now,config.quotaMaxRequests(),config.quotaWindow(),config.quotaCooldown());
  if(!quota.allowed()){boolean escalated=s.violations.record(now,config.violationBlockAfter(),config.violationWindow(),config.violationBlock());long retry=escalated?config.violationBlock().toSeconds():quota.retryAfterSeconds();d=Decision.deny(429,"ROLLING_QUOTA",retry,quota.remaining(),escalated?now.plus(config.violationBlock()):quota.resetAt(),protection,Map.of("quotaMax",config.quotaMaxRequests(),"windowSeconds",config.quotaWindow().toSeconds(),"cooldownSeconds",config.quotaCooldown().toSeconds(),"escalated",escalated));return finish(r,d,started);}
  d=Decision.allow(Math.min(burst.remaining(),quota.remaining()),quota.resetAt(),protection,Map.of("burstRemaining",burst.remaining(),"quotaRemaining",quota.remaining()));cleanup(now);return finish(r,d,started);
 }
 private Decision finish(RequestContext r,Decision d,long started){metrics.record(d);logger.log(r,d,(System.nanoTime()-started)/1000);return d;}
 private void cleanup(Instant now){long sec=now.getEpochSecond();if(sec-lastCleanupEpochSecond<300)return;lastCleanupEpochSecond=sec;Instant cutoff=now.minus(Duration.ofHours(2));clients.entrySet().removeIf(e->e.getValue().quota.lastSeen().isBefore(cutoff)&&e.getValue().violations.lastSeen().isBefore(cutoff));}
 public boolean isTrustedProxy(String ip){return ipLists.isTrustedProxy(ip);}public DefenseMetrics metrics(){return metrics;}public SurgeDetector.Snapshot surgeSnapshot(){return surge.snapshot(Instant.now());}public DefenseConfig config(){return config;}public void close(){logger.close();}
 private static final class ClientState{final TokenBucket bucket;final RollingQuota quota=new RollingQuota();final ViolationTracker violations=new ViolationTracker();ClientState(int c,double r,long now){bucket=new TokenBucket(c,r,now);}}
}
