# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.x     | ✅        |

## Reporting a Vulnerability

If you discover a security vulnerability, please report it responsibly:

1. **Do not** open a public issue
2. Email the maintainer directly or use GitHub's private vulnerability reporting
3. Include steps to reproduce and potential impact

We aim to respond within 48 hours and patch critical issues promptly.

## Scope

GeoLinkPinPoint processes GPS coordinates locally on-device. It does not transmit data to external servers. The main security-relevant areas are:
- `FileProvider` for CSV export sharing
- Location permission handling
- Intent data parsing (geo: URIs from untrusted sources)
