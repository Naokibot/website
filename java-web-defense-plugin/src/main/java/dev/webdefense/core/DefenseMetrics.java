package dev.webdefense.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class DefenseMetrics {
 private final LongAdder total=new LongAdder(),allowed=new LongAdder(),denied=new LongAdder(),logDropped=new LongAdder();
 private final ConcurrentHashMap<String,LongAdder> reasons=new ConcurrentHashMap<>();
 public void record(Decision d){total.increment();if(d.allowed())allowed.increment();else denied.increment();reasons.computeIfAbsent(d.reason(),k->new LongAdder()).increment();}
 public void logDropped(){logDropped.increment();}
 public Map<String,Object> snapshot(){Map<String,Object> out=new LinkedHashMap<>();out.put("total",total.sum());out.put("allowed",allowed.sum());out.put("denied",denied.sum());out.put("logDropped",logDropped.sum());Map<String,Long> rc=new LinkedHashMap<>();reasons.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->rc.put(e.getKey(),e.getValue().sum()));out.put("reasons",rc);return out;}
}
