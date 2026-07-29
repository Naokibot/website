package dev.webshield.cli;
import dev.webshield.api.*;import dev.webshield.core.*;import java.nio.file.*;import java.time.Instant;import java.util.Map;
public final class WebShieldCli{
 public static void main(String[]a)throws Exception{if(a.length>0&&a[0].equals("analyze")){LogAnalyzer.analyze(Path.of(a.length>1?a[1]:"logs/access.jsonl"),Path.of(a.length>2?a[2]:"reports/analysis.md"));System.out.println("Report created");return;}try(var e=new DefenseEngine(Path.of(a.length>1?a[1]:"config/webshield.properties"))){if(a.length>0&&a[0].equals("self-test")){for(int i=0;i<50;i++){Decision d=e.inspect(new WebRequest("198.51.100.10","GET","/demo",Map.of(),Instant.now()));if(i==49)System.out.println(d);}System.out.println(e.stats());}else System.out.println("WebShield plugin JAR. Commands: self-test | analyze [log] [report]");}}
}
