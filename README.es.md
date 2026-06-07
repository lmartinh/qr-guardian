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
- El wiring actual lee configuración opcional del host y sigue funcionando en modo local-only si no hay keys.

## Verificaciones de seguridad
- El bloque local valida normalización, clasificación, reglas de URL y metadata HEAD.
- La reputación remota es opcional y solo se ejecuta para URLs.
- Sin API key, QR Guardian funciona en modo local-only.

## Inyección de dependencias
- QR Guardian usa Koin para la inyección de dependencias en KMP.
- Koin solo se usa en el borde de wiring de la app.
- El dominio sigue siendo independiente del framework.
- `RemoteReputationConfig` lo proporcionan explícitamente los hosts Android e iOS.
- El modo local-only sigue siendo el comportamiento por defecto.
- `QrGuardianSecurityPipelineFactory` es la única fuente de verdad para componer el pipeline de seguridad.
- Koin delega en esa factory en lugar de duplicar la composición.

## Configuración de reputación remota
QR Guardian funciona sin API keys desde el primer momento.

- Local Scan siempre se ejecuta.
- Remote Reputation es opcional y solo se aplica a URLs.
- `GOOGLE_SAFE_BROWSING_API_KEY` activa Google Safe Browsing.
- `URLHAUS_API_KEY` activa URLhaus.
- Si no hay ninguna key configurada, la app permanece en modo local-only y la sección remota muestra `Not configured`.

Android:
- Añade las keys en `local.properties` en la raíz del proyecto.
- `local.properties.example` muestra los marcadores vacíos exactos.
- Los valores vacíos o ausentes mantienen el modo local-only.

iOS:
- Añade las keys mediante `iosApp/Configuration/RemoteReputation.xcconfig`, copiado desde `iosApp/Configuration/RemoteReputation.example.xcconfig`.
- Los valores se exponen a `Info.plist` y los lee el proveedor de configuración de iOS compartido.
- Los valores vacíos o ausentes mantienen el modo local-only.

No subas claves reales al repositorio.
Para producción, usa un backend o proxy en lugar de incrustar las keys de reputación en el binario móvil.

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
