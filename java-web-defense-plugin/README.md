# Java Web Defense Plugin

A separate Jakarta Servlet filter plugin for HTTP-layer DDoS detection and mitigation.

## Features

- Per-IP token-bucket rate limiting
- Generic rolling quota and cooldown control
- EWMA-based traffic surge detection
- Temporary strict protection mode
- IPv4/IPv6/CIDR allowlist and blacklist
- Trusted-proxy-aware client IP resolution
- Asynchronous JSONL logging
- Offline Markdown trend reports

See `README_JA.md` for Japanese setup instructions.

This project is defensive only. It contains no attack generator, scanner, exploit, or outbound load tool.
