<div align="center">
  <img src="docs/assets/qr-guardian-hero.png" alt="QR Guardian hero - escaneo seguro de QR" width="100%">
</div>

# QR Guardian

> **Leer en otro idioma:** [English](README.md) · **Español**

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS-0A7EA4)](https://developer.android.com/)
[![Package](https://img.shields.io/badge/Package-com.lmartin.qrguardian-2ea44f)](https://kotlinlang.org/docs/packages.html)

### Scan smarter. Open safer.

## ¿Qué es QR Guardian?
QR Guardian es una app móvil Kotlin Multiplatform para Android e iOS.

Escanea códigos QR y de barras, detecta el tipo de contenido escaneado y ayuda a evaluar URLs potencialmente maliciosas **antes de abrirlas**.

## ¿Por qué este proyecto?
- Construir una app móvil práctica y orientada a seguridad.
- Mostrar una arquitectura limpia con KMP + Compose para portfolio.
- Mantener lógica compartida, UI cuidada y comportamiento predecible.

## Flujo principal del producto
1. El usuario abre la app.
2. El usuario inicia el escaneo.
3. La app lee el contenido QR/código de barras.
4. La app detecta el tipo de contenido.
5. Si es URL, evalúa su seguridad.
6. La app muestra primero el resultado; el usuario decide qué hacer.

## Pantallas principales previstas
- Intro / Lanzamiento
- Captura con cámara
- Resultado

## Principios de seguridad
- No abrir URLs automáticamente.
- Mostrar advertencias claras en resultados sospechosos o maliciosos.
- Tratar resultados desconocidos como inciertos.
- No exponer secretos de proveedores de seguridad en el cliente móvil.
- Ejecutar primero el análisis local y después la reputación remota solo para URLs.
- Separar el resultado en `Local Scan` y `Remote Reputation` para la UI.
- El wiring actual de la app funciona en modo local-only por defecto salvo que el host inyecte `RemoteReputationConfig`.

## Verificaciones de seguridad
- El bloque local valida normalización, clasificación, reglas de URL y metadata HEAD.
- La reputación remota es opcional y solo se ejecuta para URLs.
- Sin API key, QR Guardian funciona en modo local-only.

## Stack técnico
- Kotlin Multiplatform
- Compose Multiplatform
- Android + iOS
- Kotlin Coroutines
- Principios de Clean Architecture

## Estructura del proyecto
- `androidApp/`: app host Android.
- `iosApp/`: app host iOS (proyecto Xcode).
- `shared/`: lógica KMP compartida y UI Compose compartida.
- `docs/`: documentación de producto, arquitectura, roadmap, seguridad y testing.

## Ejecución
Build debug Android:
```bash
./gradlew :androidApp:assembleDebug
```

iOS (desde Xcode):
`iosApp/` → abrir en Xcode y ejecutar el target.

## Tests
Tests Android host:
```bash
./gradlew :shared:testAndroidHostTest
```

Tests iOS simulador:
```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Índice de documentación
- [Overview](docs/00-overview.md)
- [Roadmap](docs/01-roadmap.md)
- [Functional Specification](docs/02-functional-specification.md)
- [Architecture](docs/03-architecture.md)
- [UI Flow](docs/04-ui-flow.md)
- [Security Model](docs/05-security-model.md)
- [Testing Strategy](docs/06-testing-strategy.md)
- [Agent Tasks](docs/07-agent-tasks.md)
- [Agent Guidelines](AGENTS.md)
