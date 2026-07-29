package dev.webshield.cli;
import java.nio.file.*;import java.util.*;
final class LogAnalyzer{
 static void analyze(Path in,Path out)throws Exception{Map<String,Integer>ips=new HashMap<>(),reasons=new HashMap<>();long total=0,denied=0;for(String l:Files.exists(in)?Files.readAllLines(in):List.<String>of()){total++;String ip=value(l,"ip"),reason=value(l,"reason");ips.merge(ip,1,Integer::sum);reasons.merge(reason,1,Integer::sum);if(l.contains("\"allowed\":false"))denied++;}StringBuilder b=new StringBuilder("# WebShield Analysis\n\n- Total: "+total+"\n- Denied: "+denied+"\n\n## Top IPs\n");ips.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(20).forEach(e->b.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append('\n'));b.append("\n## Reasons\n");reasons.forEach((k,v)->b.append("- ").append(k).append(": ").append(v).append('\n'));Files.createDirectories(out.toAbsolutePath().getParent());Files.writeString(out,b.toString());}
 private static String value(String l,String k){String x="\""+k+"\":\"";int a=l.indexOf(x);if(a<0)return "";a+=x.length();int b=l.indexOf('"',a);return b<0?"":l.substring(a,b);}
}
