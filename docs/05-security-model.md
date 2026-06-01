# Security Model

## Why Scanned URLs Can Be Dangerous
QR and barcode payloads can hide malicious links that are not obvious to users before opening. Attack vectors include phishing, malware delivery pages, credential theft and social engineering.

## Core Product Rule
The app must never open scanned links automatically.

Users always see the analysis result first and explicitly decide what to do.

## Dangerous Schemes
These schemes should be rejected or strongly blocked when URL handling is implemented:
- `javascript:`
- `file:`
- `data:`
- `intent:`

## URL Processing Rules
- Normalize URL before analysis.
- Validate structure and supported schemes.
- Keep unknown/failed analysis outcomes clearly marked as uncertain.

## Key Management Rule
Mobile apps must not expose URL reputation provider API keys.

## Recommended Production-Like Flow
Mobile app  
↓  
Backend endpoint  
↓  
URL reputation provider  
↓  
Backend normalized response  
↓  
Mobile app result screen

## Potential Providers (Future)
- Google Safe Browsing
- PhishTank
- VirusTotal
- urlscan.io

Provider integration is future work and is intentionally out of scope for this documentation task.
