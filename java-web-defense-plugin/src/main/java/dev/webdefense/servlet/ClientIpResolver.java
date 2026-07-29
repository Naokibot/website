package dev.webdefense.servlet;

import dev.webdefense.core.DefenseEngine;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public final class ClientIpResolver {
 private ClientIpResolver(){}
 public static String resolve(HttpServletRequest request,DefenseEngine engine,String forwardedHeader){String remote=normalize(request.getRemoteAddr());if(!engine.isTrustedProxy(remote))return remote;String forwarded=request.getHeader(forwardedHeader);if(forwarded==null||forwarded.isBlank())return remote;String first=forwarded.split(",",2)[0].trim();return isIp(first)?normalize(first):remote;}
 private static boolean isIp(String v){try{InetAddress.getByName(v);return v.matches("[0-9a-fA-F:.]+");}catch(Exception e){return false;}}
 private static String normalize(String v){if(v==null)return "0.0.0.0";if(v.startsWith("[")&&v.endsWith("]"))return v.substring(1,v.length()-1);return v;}
}
