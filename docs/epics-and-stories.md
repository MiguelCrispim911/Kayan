Epics y Historias — Kayab: Hijo del Trueno (MVP)
===============================================

Contexto rápido
- Plataforma: Android (Android Studio)
- Stack: Java + LibGDX + Box2D
- Modo prototipo: Fase 1 con ShapeRenderer y Box2DDebugRenderer; Fase 2 sprites
- Referencia: repo del profesor `jumpdontdie-master` (archivos listados abajo)

Nota: planificación extendida a 2 semanas; el desarrollo será asistido por IA para scaffolding, tests y generación básica de assets. Ver sección "Tareas IA" en las historias.

E1 — Núcleo jugador y controles (Must)
-------------------------------------
Descripción: Implementar player, input y cámara para un nivel jugable.

1.1 Control del jugador (movimiento y salto)
- Criterios de aceptación: el jugador responde a input táctil/teclado; movimiento horizontal y salto con física Box2D; solo ShapeRenderer/Box2D debug.
- Estimación: 6h
- Prioridad: Must
- Dependencias: ninguna
- Reutilizable: `core/.../entities/PlayerEntity.java`

1.2 Cámara que sigue al jugador (scroll derecha)
- Criterios: cámara sigue a Kayab hacia derecha; no retrocede; suavizado básico; funciona con debug rendering.
- Estimación: 3h
- Prioridad: Must
- Dependencias: 1.1
- Reutilizable: `core/.../game/GameScreen.java`

1.3 Input táctil: joystick virtual + botón disparo (placeholder)
- Criterios: controles táctiles mapeados a acciones; teclado funciona en Desktop; documentación corta de controles.
- Estimación: 4h
- Prioridad: Must
- Dependencias: 1.1
- Reutilizable: patrones en `MainGame.java` y `PlayerEntity.java`

1.4 Integración Box2D World + debug renderers
- Criterios: `World` creado y actualizado; `Box2DDebugRenderer` renderiza cuerpos; `ShapeRenderer` para pruebas.
- Estimación: 4h
- Prioridad: Must
- Dependencias: 1.1
- Reutilizable: `box2d/Box2DScreen.java`, `BodyDefFactory.java`, `FixtureFactory.java`

Tareas IA (ejemplos aplicables a E1)
- Generar scaffolding de `Player` y `TouchControls` (prompt-driven). Revisar y adaptar.
- Generar casos de prueba manuales y scripts de QA para controles.

E2 — Física, colisiones y objetos de nivel (Must)
------------------------------------------------
2.1 Plataformas y suelo
- Criterios: jugador apoya y detecta suelo; saltos habilitados; sin penetraciones.
- Estimación: 5h
- Dependencias: E1.1, E1.4
- Reutilizable: `FixtureFactory.java`, `EntityFactory.java`

2.2 Flechas como proyectiles
- Criterios: flechas spawn y se mueven horizontalmente; desaparecen al superar rango o al impactar; colisionan con jugador.
- Estimación: 5h
- Dependencias: 2.1
- Reutilizable: `EntityFactory.java` (patrón de creación)
 - Nota técnica: valores por defecto — jugador: 200px (≈6.25m PPM=32), enemigos: 150px (≈4.7m). Ajustar `Constants.ARROW_MAX_RANGE` y `Constants.ENEMY_ARROW_RANGE`.

Tareas IA (E2)
- Generar plantilla de `Arrow` y tests de alcance/vida útil.
- Sugerir parámetros de físicas y crear una matriz de tuning inicial.

2.3 ContactListener y gestión de eventos
- Criterios: colisiones detectadas y manejadas (golpes, triggers); logs de pruebas.
- Estimación: 3h
- Dependencias: 2.1, 2.2
- Reutilizable: `GameScreen.java` (GameContactListener)

E3 — Enemigos y comportamiento (Must)
-------------------------------------
3.1 Enemigo patrulla
- Criterios: patrulla entre puntos, detección de borde, colisiones, debug visible.
- Estimación: 4h
- Dependencias: E1,E2
- Reutilizable: `EntityFactory.java`, `PlayerEntity.java`
 - Nota técnica: la detección de borde puede implementarse leyendo propiedades del tilemap (por ejemplo la propiedad `invertOnEdge` en Tiled) o por límites X definidos en el spawn; documentar la decisión en `Level`.

3.2 Enemigo que dispara
- Criterios: dispara flechas en intervalo; flechas con físicas; daño al jugador.
- Estimación: 6h
- Dependencias: 3.1, 2.2
- Reutilizable: `EntityFactory.java`

Tareas IA (E3)
- Generar código inicial de patrones de patrulla y un ejemplo de AI paramétrico (intervalos, rango de detección).
- Generar scripts de test para comprobar spawn y destrucción.
 - Preferir en la implementación el uso de la propiedad Tiled `invertOnEdge` cuando el nivel la exponga; documentar fallback en código.

3.3 Salud y muerte de entidades
- Criterios: HP, muerte y limpieza segura del body.
- Estimación: 3h
- Dependencias: 3.1–3.2
- Reutilizable: `PlayerEntity.java` (ejemplo de flags y detach)

E4 — Level flow y guardado (Must)
4.1 Niveles 1–4 (layout en código o JSON simple)
- Criterios: mapa con plataformas y spawns de enemigos/flechas; jugable de principio a fin.
- Estimación: 5h
- Dependencias: E1–E3
- Reutilizable: `GameScreen.java`, `EntityFactory.java`

4.1.1 Nivel 1 — Mundo 1: La Tierra del Trueno (Tutorial)
- Criterios: recorrido de 4–5 minutos; enemigos sencillos; checkpoint inicial.
- Estimación adicional: 4h

4.1.2 Nivel 2 — Mundo 2: El Pacto de Hierro
- Criterios: plataformas móviles, mayor densidad de enemigos, primeros puzzles de timing.
- Estimación adicional: 6h

4.1.3 Nivel 3 — Mundo 3: El Corazón del Imperio
- Criterios: enemigos ranged y mayor complejidad; Boss Xocotl con dos fases.
- Estimación adicional: 8h

4.1.4 Nivel 4 — Mundo 4: La Traición
- Criterios: densidad alta de enemigos, patrones combinados y Boss Vargas con segunda fase y cinemática final.
- Estimación adicional: 10h

4.2 Guardado de progreso (SQLite/SharedPreferences)
- Criterios: guardar al completar checkpoint o boss; restaurar progreso al reabrir.
- Estimación: 3h
- Dependencias: 4.1
- Reutilizable: patrón de inicialización en `MainGame.java` (nueva clase `DatabaseManager` en Android module)

4.3 Reinicio y UI rápida de retry
- Criterios: reinicio desde checkpoint, pantalla de GameOver integrada.
- Estimación: 2h
- Dependencias: 4.2

4.4 Pantallas y cinemáticas
- Descripción: implementar pantallas de menú/nombre y sistema de cinemáticas para narrativa.

4.4.1 NewGame / Menu Screen (US17)
- Criterios: pantalla principal con Nueva Partida/Continuar, ingreso de nombre y creación de save.
- Estimación: 3h
- Prioridad: Must
- Dependencias: 4.2 (guardado)

4.4.2 CinematicScreen (US16)
- Criterios: reproducir secuencias de imagen/texto con callbacks; pausa de input del jugador.
- Estimación: 4h
- Prioridad: Should
- Dependencias: US11 (HUD) opcional

E5 — Arte, HUD y transición a sprites (Should)
---------------------------------------------
5.1 Reemplazo de sprites (fase 2)
- Criterios: `SpriteBatch` y `Texture` sustitutivos manteniendo físicas; funciona en Android y Desktop.
- Estimación: 6h
- Dependencias: E1–E4
- Reutilizable: `MainGame.java` (AssetManager pattern)

Tareas IA (E5)
- Generar pixel art base (sprites 16×32) y variantes para animaciones simples.
- Generar HUD mockups y patches para `uiskin`.

5.2 HUD: vida y puntuación
- Criterios: overlay que muestra vida y score y se actualiza en tiempo real.
- Estimación: 3h
- Dependencias: 5.1
- Reutilizable: assets de UI en repo ejemplo (uiskin)

5.3 Animaciones básicas
- Criterios: animaciones por estado (idle/run/shoot).
- Estimación: 4h
- Dependencias: 5.1

E6 — Pulido y QA rápido (Could)
-------------------------------
6.1 Tuning de físicas y parámetros
- Estimación: 4h
6.2 Build y prueba en Android (APK debug)
- Estimación: 3h

Resumen de prioridades
- MVP (Must): E1, E2, E3, E4
- Visual/UX (Should): E5
- Pulido (Could): E6

Archivos/clases reutilizables (rutas relevantes)
- `core/src/es/danirod/jddprototype/game/MainGame.java`
- `core/src/es/danirod/jddprototype/game/GameScreen.java`
- `core/src/es/danirod/jddprototype/game/entities/PlayerEntity.java`
- `core/src/es/danirod/jddprototype/game/entities/EntityFactory.java`
- `core/src/es/danirod/jddprototype/box2d/Box2DScreen.java`
- `core/src/es/danirod/jddprototype/box2d/BodyDefFactory.java`
- `core/src/es/danirod/jddprototype/box2d/FixtureFactory.java`
- `core/src/es/danirod/jddprototype/game/Constants.java`

Tareas técnicas recomendadas para Day 1 (arranque)
- Clonar/importar y abrir `jumpdontdie-master` en Android Studio.
- Ejecutar build/run en Desktop para confirmar entorno.
- Crear branch `feature/kayab-mvp-day1`.
- Reutilizar `MainGame` para inicialización y `GameScreen` como base.
- Implementar `PlayerEntity` minimal (mover + salto) y validar con `Box2DDebugRenderer`.
- Añadir `ShapeRenderer` para pruebas visuales y una plataforma simple.
- Implementar cámara que sigue a player (scroll derecha sin retroceso).
- Mapear inputs (teclado + touch placeholders).
- Checklist Day1: movement, jump, camera follow, debug render.

Entregables por historia
- Código con tests manuales y nota en PR describiendo cómo probar cada historia.

Siguiente acción que puedo hacer ahora
- Guardar este backlog en `epics-and-stories.md` en la carpeta del PRD (ya lo guardaré si confirmas).  
- O generar el plan día a día desglosado ahora mismo.
