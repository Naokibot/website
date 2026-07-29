# Java DDoS Web Plugin v2

A defensive-only Java 21 library that can be embedded before your web request handler.

## Features
- Per-IP token-bucket rate limit
- Rolling usage quota and cooldown (similar in concept to free-tier usage controls; not a clone of any private service)
- EWMA/absolute traffic surge detection and temporary stricter limits
- Exact-IP allowlist and blacklist
- Repeated-violation temporary blocking
- JSONL audit logging and Markdown analysis report
- Framework-neutral `WebDefensePlugin` API

## Build
```bash
mvn clean package
```

## Core integration
```java
var plugin = new DefenseEngine(Path.of("config/webshield.properties"));
var decision = plugin.inspect(new WebRequest(clientIp, method, path, headers, Instant.now()));
if (!decision.allowed()) {
  // Return HTTP 429 and Retry-After: decision.retryAfterSeconds()
  return;
}
// Continue normal web processing.
```

## Commands
```bash
java -jar java-ddos-web-plugin-v2.jar self-test
java -jar java-ddos-web-plugin-v2.jar analyze logs/access.jsonl reports/analysis.md
```

## Security scope
This is application-layer mitigation. Put a CDN/cloud DDoS service and TLS reverse proxy in front of public production systems. Do not trust `X-Forwarded-For` unless the request came from a configured trusted proxy. This project contains no attack, scanning, flooding, or exploitation capability.
