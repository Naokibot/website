package dev.webshield.api;
public record Decision(boolean allowed,int status,String reason,long retryAfterSeconds,long remainingQuota,boolean surgeMode){
 public static Decision allow(long remaining,boolean surge){return new Decision(true,200,"ALLOWED",0,remaining,surge);}
 public static Decision deny(String reason,long retry,boolean surge){return new Decision(false,429,reason,Math.max(1,retry),0,surge);}
}
