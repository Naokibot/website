package dev.webdefense.core;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public record DefenseConfig(boolean enabled,int rateCapacity,double rateRefillPerSecond,int quotaMaxRequests,
 Duration quotaWindow,Duration quotaCooldown,int surgeBucketSeconds,long surgeMinimumRps,double surgeMultiplier,
 double surgeEwmaAlpha,Duration surgeProtection,int surgeStrictCapacity,double surgeStrictRefillPerSecond,
 int violationBlockAfter,Duration violationWindow,Duration violationBlock,Path allowlistPath,Path blacklistPath,
 Path trustedProxiesPath,Duration listReload,String forwardedHeader,Path logPath,int logQueueCapacity,
 boolean logIncludeAllowed,boolean logHashIp,String logHashSalt,int logRetentionDays,int responseRetryAfterSeconds,
 String responseMessage,boolean addRateLimitHeaders,List<String> exemptPaths,boolean adminEnabled,String adminPath,String adminToken){
 public static DefenseConfig load() throws IOException{
  Properties p=new Properties();
  try(InputStream in=DefenseConfig.class.getResourceAsStream("/web-defense.properties")){if(in!=null)p.load(in);}
  String external=System.getProperty("web.defense.config",System.getenv("WEB_DEFENSE_CONFIG"));
  if(external!=null&&!external.isBlank())try(InputStream in=Files.newInputStream(Path.of(external))){p.load(in);}
  String token=System.getProperty("web.defense.adminToken",System.getenv("WEB_DEFENSE_ADMIN_TOKEN")); if(token==null)token="";
  return new DefenseConfig(bool(p,"plugin.enabled",true),integer(p,"rate.capacity",60),decimal(p,"rate.refillPerSecond",2),
   integer(p,"quota.maxRequests",500),seconds(p,"quota.windowSeconds",1800),seconds(p,"quota.cooldownSeconds",600),
   integer(p,"surge.bucketSeconds",1),integer(p,"surge.minimumRequestsPerSecond",100),decimal(p,"surge.multiplier",4),
   decimal(p,"surge.ewmaAlpha",.2),seconds(p,"surge.protectionSeconds",120),integer(p,"surge.strictCapacity",15),
   decimal(p,"surge.strictRefillPerSecond",.5),integer(p,"violations.blockAfter",5),seconds(p,"violations.windowSeconds",120),
   seconds(p,"violations.blockSeconds",900),Path.of(p.getProperty("allowlist.path","config/allowlist.txt")),
   Path.of(p.getProperty("blacklist.path","config/blacklist.txt")),Path.of(p.getProperty("trustedProxies.path","config/trusted-proxies.txt")),
   seconds(p,"lists.reloadSeconds",15),p.getProperty("clientIp.forwardedHeader","X-Forwarded-For"),
   Path.of(p.getProperty("log.path","logs/web-defense.jsonl")),integer(p,"log.queueCapacity",20000),
   bool(p,"log.includeAllowed",true),bool(p,"log.hashIp",false),p.getProperty("log.hashSalt","change-this-in-production"),
   integer(p,"log.retentionDays",30),integer(p,"response.retryAfterSeconds",30),p.getProperty("response.message","Too Many Requests"),
   bool(p,"response.addRateLimitHeaders",true),Arrays.stream(p.getProperty("exempt.paths","/health,/actuator/health,/favicon.ico").split(",")).map(String::trim).filter(s->!s.isEmpty()).toList(),
   bool(p,"admin.enabled",false),p.getProperty("admin.path","/__web-defense/status"),token);
 }
 private static boolean bool(Properties p,String k,boolean d){return Boolean.parseBoolean(p.getProperty(k,Boolean.toString(d)));}
 private static int integer(Properties p,String k,int d){try{return Integer.parseInt(p.getProperty(k,Integer.toString(d)).trim());}catch(Exception e){throw new IllegalArgumentException("Invalid "+k,e);}}
 private static double decimal(Properties p,String k,double d){try{return Double.parseDouble(p.getProperty(k,Double.toString(d)).trim());}catch(Exception e){throw new IllegalArgumentException("Invalid "+k,e);}}
 private static Duration seconds(Properties p,String k,int d){return Duration.ofSeconds(integer(p,k,d));}
}
