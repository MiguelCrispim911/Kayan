---
title: "PRD — Kayab: Hijo del Trueno (Demo)"
status: draft
created: 2026-06-06
updated: 2026-06-06
author: Migue
project: Kayab
---

Resumen
-------
Kayab: Hijo del Trueno es un demo de plataformas 2D (4 mundos, 1 nivel cada uno) desarrollado en Android Studio con Java, LibGDX y Box2D. Objetivo: entregar un prototipo jugable y pulido visualmente en formato demo cinematográfico.

- Mecánica básica de movimiento y salto (joystick virtual) y disparo horizontal.
- Enemigos con patrulla horizontal y disparo limitado. 2 jefes con 2 fases.
- Scroll unidireccional (derecha) y mapas TMX cargados con `TmxMapLoader`.
- Guardado básico en SQLite (tabla `saves`).
- Prototipo visual en Fase 1 con `ShapeRenderer` + `Box2DDebugRenderer`; Fase 2: reemplazo por sprites.
- Pantalla de ingreso de nombre (`NewGameScreen`) y menú principal (Nueva partida / Continuar).

Objetivos de éxito
------------------
- Vertical slice jugable (nivel completo: correr, plataformas, enemigos, boss).
- Estabilidad: físicas y colisiones confiables; cero regressions al cambiar sprites.
- Tiempo: Entrega en 2 semanas (plan escalonado, ver sección "Cronograma extendido").

Público objetivo
-----------------
Evaluación académica (profesor), demo para jugadores móviles (Android).

Requisitos funcionales clave
---------------------------
FR-1: Controles táctiles: joystick izquierdo (mover/saltar) y botón derecho (disparo).
FR-2: Flechas viajan horizontalmente y desaparecen tras rango máximo (valores por defecto: jugador = 200px, enemigos = 150px).
FR-3: Enemigos patrullan en eje X, detectan rango y disparan.
FR-4: Cámara sigue a Kayab a la derecha; el jugador no puede retroceder más allá del límite mostrado.
FR-5: Guardado automático por mundo/derrota de jefe en SQLite.

Requisitos no funcionales
------------------------
- Rendimiento: 60 FPS objetivo en dispositivos medianos de Android.
- Tamaño APK razonable (< 50MB con sprites básicos).
- Código modular: `core` portable entre desktop/android (libGDX multi-backend).

Stack tecnológico propuesto
--------------------------
- LibGDX (core)
- Box2D para física
- Scene2D/Stage para UI y pantallas
- SQLite para persistencia (implementación Android)
- AssetManager para carga de assets

Arquitectura propuesta (resumen)
--------------------------------
- `MainGame` (`Game`) con `LoadingScreen`, `MenuScreen`, `GameScreen`.
- Entidades: `Player`, `Enemy` (enum `EnemyType`), `Boss`, `Arrow`.
- `TouchControls` separado para input táctil.
- `DatabaseManager` en módulo Android implementando la interfaz `IDatabase` en `core`.

Cronograma extendido (2 semanas)
--------------------------------
Semana 1 — Fundamentos y vertical slice funcional
- Día 1: Configuración del repo, build Desktop, rama `feature/kayab-mvp-week1`, `MainGame` + `GameScreen` base.
- Día 2: Player (movimiento/salto) + Box2D world + debug render.
- Día 3: Cámara scroll unidireccional + TouchControls (joystick placeholder).
- Día 4: Plataformas, suelo, y ContactListener básico.
- Día 5: Flechas, spawn y física de proyectiles; enemigo patrulla básico.

Semana 2 — Completar niveles, IA y pulido visual
- Día 6: Implementar enemigos con disparo y lógica de HP; boss simple (fase 1).
- Día 7: Nivel 1 jugable de principio a fin; checkpoints y guardado SQLite.
- Día 8: Nivel 2: aumentar dificultad (más enemigos, plataformas móviles, spawns).
- Día 9: Nivel 3: enemigos ranged, mayor densidad y boss Xocotl.
- Día 10: Nivel 4: enemigos españoles, boss Vargas, cinemática final y export.
- Día 11: Fase visual: reemplazo por sprites, HUD y animaciones básicas.
- Día 12: Integración de assets generados por IA, pruebas en dispositivo, optimizaciones.

Nota: el calendario incluye tiempo para generación/edición asistida por IA (scaffolding de código, pruebas unitarias, generación de pixel art básico y diálogos), sesiones de integración y al menos una jornada de pruebas en dispositivo físico.

Niveles y dificultad (visión)
-----------------------------
- Mundo 1 — La Tierra del Trueno (tutorial suave): plataformas simples, pocos enemigos, objetivo: aprender controles.
- Mundo 2 — El Pacto de Hierro (incremento): plataformas móviles, más enemigos, primeros retos de alcance de flechas.
- Mundo 3 — El Corazón del Imperio (avanzado): enemigos ranged, puzzles de plataformas y Boss Xocotl (dos fases).
- Mundo 4 — La Traición (final): mayor densidad de enemigos, combinaciones de patrones y Boss Vargas con segunda fase y cinemática final.

Desarrollo asistido por IA
-------------------------
Se usará IA para acelerar la implementación en las siguientes tareas (ejemplos):
- Generar plantillas y scaffolding de clases (`Player`, `Arrow`, `Enemy`, `Level`).
- Generar tests unitarios y scripts de prueba manuales.
- Producir pixel art y sprites básicos (por prompts) y variantes de animación simples.
- Generar mapas TMX de ejemplo a partir de descripciones textuales.
- Escribir diálogos y textos de cinemáticas a partir del GDD.

Reglas de uso de IA
-------------------
- Todas las salidas de IA deben revisarse manualmente por un desarrollador antes de integrarlas.
- No integrar activos que violen derechos de autor; usar prompts para generar arte original o assets con licencia libre.
- Registrar en `.decision-log.md` todas las decisiones clave y las fuentes de IA usadas.

Epics iniciales
--------------
- E1: Core jugador y controles
- E2: Enemigos y AI básica
- E3: Sistema de flechas y colisiones
- E4: Mapas TMX y scroll
- E5: Guardado y persistencia
- E6: UI, HUD y cinemáticas

Riesgos y supuestos
-------------------
- Supuesto: Uso de Box2D del ejemplo Jump Don't Die como referencia reduce tiempo de integración.
- Riesgo: Problemas de rendimiento en dispositivos bajos al usar múltiples cuerpos Box2D; mitigación: optimizar fixtures y limitar cuerpos activos.

Próximos pasos inmediatos
------------------------
1. Revisar el código de `jumpdontdie-master` para mapear clases reutilizables.
2. Generar lista de historias (tickets) priorizadas por MVP.
3. Empezar implementación del Day 1 (player + controls + nivel prototipo).

Fuentes
-------
- `inputs/KAYAB_GDD_SIMPLIFICADO.md`
- `inputs/KAYAB_VS_JDD_COMPARISON.md`
- Ejemplo: `../jumpdontdie-master/jumpdontdie-master` (repo del profesor)
