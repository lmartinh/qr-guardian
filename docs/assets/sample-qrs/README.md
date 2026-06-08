# QR Guardian Sample QR Dataset

This dataset is synthetic. It is intended for manual QA, demos, screenshots, and portfolio walkthroughs.

The QR image files are not automated decoding fixtures. Automated regression tests validate the underlying text payloads in shared Kotlin tests, especially `QrSampleDatasetRegressionTest` and `QrSampleCases`.

Do not replace these samples with real malicious domains. The current payloads are controlled demo values that exercise Local Scan, metadata inference, result rendering, and optional Remote Reputation behavior.

Remote Reputation results may vary depending on provider configuration and live provider responses. The table below describes the expected local behavior.

## Preview

![QR Guardian sample QR contact sheet](./contact-sheet.png)

## Samples

| File | Stored content | Purpose | Expected local result |
|---|---|---|---|
| `qrs/01_safe_https.png` | `https://example.com` | Safe HTTPS URL | Safe |
| `qrs/02_bare_domain.png` | `example.com` | Bare domain normalization | Suspicious |
| `qrs/03_http_url.png` | `http://example.com` | Non-HTTPS URL | Suspicious |
| `qrs/04_at_symbol_url.png` | `https://google.com@evil.example/login` | Obfuscated destination with `@` | Suspicious |
| `qrs/05_ip_host.png` | `http://192.168.1.20/login` | IP-host URL | Suspicious |
| `qrs/06_shortener.png` | `https://bit.ly/qr-guardian-test` | Shortened URL | Suspicious |
| `qrs/07_dangerous_apk.png` | `https://example.com/download/app.apk` | APK download | Dangerous |
| `qrs/08_windows_exe.png` | `https://example.com/setup.exe` | EXE download | Dangerous |
| `qrs/09_pdf_menu.png` | `https://example.com/menu.pdf` | PDF/menu file detection | Safe |
| `qrs/10_archive_zip.png` | `https://example.com/archive.zip` | Archive download | Suspicious |
| `qrs/11_many_params.png` | `https://example.com/path?a=1&b=2&c=3&d=4&e=5&f=6&g=7&h=8&i=9` | Excessive query params | Suspicious |
| `qrs/12_brand_impersonation.png` | `https://paypal-secure-login.example.com` | Brand impersonation pattern | Suspicious |
| `qrs/13_many_subdomains.png` | `https://login.security.account.example.com` | Many subdomains | Suspicious |
| `qrs/14_dangerous_scheme_js.png` | `javascript:alert('qr')` | Unsafe JavaScript scheme | Dangerous |
| `qrs/15_data_scheme.png` | `data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==` | Unsafe data scheme | Dangerous |
| `qrs/16_wifi_open.png` | `WIFI:T:nopass;S:OpenCafeWiFi;;` | Open WiFi QR | Suspicious |
| `qrs/17_wifi_wpa.png` | `WIFI:T:WPA;S:DemoNetwork;P:DemoPassword123;;` | Passworded WiFi QR | Suspicious |
| `qrs/18_sms_with_url.png` | `sms:+34000000000?body=Please verify your account https://example.com/login` | SMS action with URL in body | Suspicious |
| `qrs/19_mailto_prefilled.png` | `mailto:test@example.com?subject=Account%20Update&body=Please%20confirm%20your%20data` | Prefilled email action | Suspicious |
| `qrs/20_plain_text.png` | `Hello from QR Guardian sample.` | Plain text fallback | Unknown |
| `qrs/21_crypto.png` | `bitcoin:bc1qexampleaddress000000000000000000000000000?amount=0.01` | Crypto payment URI | Suspicious |
| `qrs/22_vcard.png` | `BEGIN:VCARD ... END:VCARD` | Contact card payload | Suspicious |
| `qrs/23_geo.png` | `geo:40.4168,-3.7038` | Location payload | Suspicious |
| `qrs/24_google_safe_browsing_test_malware.png` | `http://malware.testing.google.test/testing/malware/` | Google Safe Browsing malware test URL | Suspicious locally; malicious when Google Safe Browsing is configured and reports it |

## Notes

- Local Scan always runs.
- Remote Reputation is optional and URL-only.
- Non-URL payloads show `NotApplicable` for Remote Reputation.
- Automated tests validate the text payload behavior, not QR image decoding.
- Keep sample names and table entries aligned with files in `qrs/`.
