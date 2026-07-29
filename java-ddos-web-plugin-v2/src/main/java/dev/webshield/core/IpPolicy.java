package dev.webshield.core;
import java.nio.file.*;import java.util.*;
final class IpPolicy{
 private volatile Set<String> allow=Set.of(),deny=Set.of();private final Config c;
 IpPolicy(Config c){this.c=c;reload();}
 void reload(){allow=read(c.allowlist());deny=read(c.blacklist());}
 boolean allowed(String ip){return allow.contains(ip);}boolean denied(String ip){return deny.contains(ip);}
 private static Set<String> read(Path p){try{if(!Files.exists(p))return Set.of();Set<String>s=new HashSet<>();for(String l:Files.readAllLines(p)){l=l.trim();if(!l.isEmpty()&&!l.startsWith("#"))s.add(l);}return Set.copyOf(s);}catch(Exception e){return Set.of();}}
}
