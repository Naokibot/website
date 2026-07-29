package dev.webdefense.core;

import java.time.Instant;
import java.util.*;

public final class JsonUtil {
 private JsonUtil(){}
 public static String toJson(Object v){
  if(v==null)return "null"; if(v instanceof String s)return quote(s); if(v instanceof Number||v instanceof Boolean)return v.toString(); if(v instanceof Instant i)return quote(i.toString());
  if(v instanceof Map<?,?> m){StringBuilder b=new StringBuilder("{");boolean first=true;for(var e:m.entrySet()){if(!first)b.append(',');first=false;b.append(quote(String.valueOf(e.getKey()))).append(':').append(toJson(e.getValue()));}return b.append('}').toString();}
  if(v instanceof Collection<?> c){StringBuilder b=new StringBuilder("[");boolean first=true;for(Object x:c){if(!first)b.append(',');first=false;b.append(toJson(x));}return b.append(']').toString();}
  return quote(String.valueOf(v));
 }
 public static String quote(String s){StringBuilder b=new StringBuilder("\"");for(int i=0;i<s.length();i++){char c=s.charAt(i);switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}return b.append('"').toString();}
}
