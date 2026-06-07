Plan de 2 semanas — Plan día a día (Kayab: Hijo del Trueno)
============================================================
Resumen
- Duración: 12 jornadas de desarrollo (2 semanas útiles). Cada día es una unidad ejecutable.
- Objetivo: entregar el MVP completo (4 mundos, dificultad incremental, guardado, bosses, HUD) con desarrollo asistido por IA donde aplique.
- Rama base: `feature/kayab-mvp-week1` → crear ramas específicas por historia `usXX/short-desc`.

Formato por día
- Tareas: lista técnica ligada a USxx.
- Entregable: PR WIP o PR listo según la tarea.
- QA: pasos mínimos de prueba al final del día.

Semana 1 — Fundamentos y vertical slice funcional

Día 1 — Entorno, ramas y scaffold
- Tareas:
  - Abrir/importar `jumpdontdie-master` en Android Studio.
  - Ejecutar build/run Desktop (confirmar que el ejemplo corre).
  - Crear rama `feature/kayab-mvp-week1` y sub-branch `us01-setup`.
  - Preparar prompts y checklist para scaffolding (ejecutar generación y revisión en Día 2).
  - Implementar pantalla básica de menú/ingreso de nombre `MenuScreen`/`NewGameScreen` (US17): campo nombre y botones Nueva/Continuar (usar Scene2D).
  - Añadir `README_DEV.md` con comandos de build/run.
- Entregable: PR WIP con scaffolding y pasos reproducibles.
- QA: Ejecutar app Desktop, confirmar pantalla inicial; registrar issues.

Día 2 — US01: Player movimiento y salto + US04: World
- Tareas:
  - Implementar/adaptar `PlayerEntity` (US01): body, fixtures, `jump()`, `setLinearVelocity`.
  - Crear `World` y `Box2DDebugRenderer` con fixed timestep (US04).
  - Añadir `ShapeRenderer` y colores del TDD para debug.
- Entregable: PR `us01-player` con instrucciones de prueba.
- QA: 5 runs manuales de movimiento/salto, confirmar no doble salto.

Día 3 — US02: Cámara y US03: TouchControls placeholder
- Tareas:
  - Implementar cámara que sigue a la derecha, clamp izquierdo para evitar retroceso (US02).
  - Implementar `TouchControls` placeholder (joystick knob que produce flags) + teclado fallback (US03).
  - Integrar controles con `Player`.
- Entregable: PR `us02-camera-us03-input`.
- QA: Probar control táctil con emulador/desktop, validar cámara pegada al borde.

Día 4 — US05: Plataformas y suelo + ContactListener básico
- Tareas:
  - Implementar creación de plataformas y suelo (usando `EntityFactory` y fixtures).
  - Sensor de suelo y ContactListener para `onGround` (usar patrón de `GameContactListener`).
- Entregable: PR `us05-platforms`.
- QA: Saltar varias plataformas, verificar detección y sin penetraciones.

Día 5 — US06: Flechas jugador (proyectiles) + limpieza
- Tareas:
  - Implementar clase `Arrow` (bullet=true, gravityScale=0) con rango y `active` flag.
  - Spawn de flecha desde `Player` y destrucción segura de bodies.
  - Documentar cómo ajustar `ARROW_MAX_RANGE` y `ARROW_SPEED` en `Constants`.
- Entregable: PR `us06-arrows`.
- QA: Probar disparo, rango y colisión con plataformas (sin fallo).

Semana 2 — Completar niveles, IA y pulido visual

Día 6 — US07: Enemigos patrulla y disparo básico
- Tareas:
  - Implementar `Enemy` base con `EnemyType`, patrulla y cambio de dirección.
  - Añadir disparo básico (spawn `Arrow`) cuando jugador en rango.
- Entregable: PR `us07-enemies`.
- QA: Spawnear enemigos en mapa de prueba, probar patrulla y disparo.

Día 7 — US08 + US09: Salud y Nivel 1 funcional + Guardado básico
- Tareas:
  - Añadir `hp` y `applyDamage` en `Player` y `Enemy`. Implementar `detach()` limpio.
  - Diseñar Level 1 (JSON o clase) con spawns y checkpoint. Implementar `DatabaseManager` minimal (Android) y fallback `Preferences` para Desktop.
- Entregable: PR `us08-us09-level1`.
- QA: Jugar Level1, morir y confirmar guardado al checkpoint; reiniciar y continuar.

Día 8 — US10.n: Nivel 2 diseño e implementación
- Tareas:
  - Crear Level 2 con plataformas móviles y mayor densidad de enemigos; parametrizar spawns.
  - Ajustar `Enemy` parámetros para dificultad incremental.
- Entregable: PR `us10-level2`.
- QA: Jugar Level2, validar incremento de dificultad y performance.

Día 9 — US10.n: Nivel 3 + Boss Xocotl (fase 1)
- Tareas:
  - Implementar Level 3 con enemigos ranged y posicionamiento crítico.
  - Implementar Boss Xocotl fase 1 (onda de choque) como entidad paramétrica.
- Entregable: PR `us10-level3-boss-xocotl`.
- QA: Validar patrón de boss y windows de ataque (1.5s) para disparo del jugador.

Día 10 — US10.n: Nivel 4 + Boss Vargas y cinemática
- Tareas:
  - Implementar Level 4 con mayor densidad y Boss Vargas (fase 2: pistola+espada).
  - Implementar `CinematicScreen` para cinemática final y textos del GDD (US16).
  - Añadir ejemplos de cinemática final y test de skip/return.
- Entregable: PR `us10-level4-boss-vargas`.
- QA: Jugar Level4, derrotar boss y verificar cinemática final.

Día 11 — US11 + US12: HUD y fase visual parcial
- Tareas:
  - Implementar `HUD` con corazones, score y barra de boss (Scene2D).
  - Reemplazar render debug por sprites básicos generados por IA (player, enemy, arrow, tiles). Cargar con `AssetManager`.
- Entregable: PR `us11-us12-hud-sprites`.
- QA: Verificar que sprites no cambian la física; HUD muestra valores correctos.

Día 12 — US13–US15: Pulido IA, QA final y build APK
- Tareas:
  - Integrar prompts y outputs IA en `ai/` (scaffolding, sample pixel art, TMX generator). Registrar todo en `.decision-log.md`.
  - Ejecutar checklist QA (US14) y corregir bugs críticos.
  - Build APK debug, probar en dispositivo y recopilar feedback.
- Entregable: PR final `release/mvp-v1` o merge PRs abiertos y APK debug en `_bmad-output/implementation-artifacts`.
- QA: Instalación en dispositivo, pruebas de control táctil, performance 60fps objetivo mínimo en dispositivo de referencia.

Notas operativas y recomendaciones
- Branching: ramas por US: `us01-player`, `us02-camera`, etc. PRs cortos y frecuentes.
- Commits: mensajes claros `US01: player movement — add jump(), fixture, tests`.
- Uso de IA: siempre revisar outputs antes de merge; registrar prompts y resultados en `ai/` y `.decision-log.md`.
- Testing: cada PR debe incluir una breve lista de pruebas manuales en la descripción.

Archivos generados sugeridos
- `_bmad-output/planning-artifacts/prds/.../` : mantiene PRD, epics, user-stories y week-plan.
- `ai/prompts/` : prompts canon para scaffolding y assets.
- `docs/QA_CHECKLIST.md` : checklist por historia.

Fin del plan. Si quieres, empiezo creando la rama `us01-player` y un commit scaffold para US01.
