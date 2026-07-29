package dev.webshield.api;
public record PluginStats(long total,long allowed,long denied,long blacklisted,long rateLimited,long quotaLimited,long surgeLimited,boolean surgeMode,double currentRps,double baselineRps){}
