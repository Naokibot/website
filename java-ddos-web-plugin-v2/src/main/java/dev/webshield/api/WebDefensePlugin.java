package dev.webshield.api;
public interface WebDefensePlugin extends AutoCloseable{
 Decision inspect(WebRequest request);
 PluginStats stats();
 void reload();
 @Override void close();
}
