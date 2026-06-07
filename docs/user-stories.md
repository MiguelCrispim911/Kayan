User Stories (formato Copilot-friendly)
========================================

Instrucciones de uso rápido:
- Di a Copilot: "vamos a hacer la historia US01".
- Copilot debe abrir `user-stories.md` y ejecutar la plantilla de prompt que acompaña la historia.
- Cada historia incluye: objetivo de negocio, criterios de aceptación (test manual/automatizable), tareas técnicas desglosadas, archivos reutilizables del repo ejemplo, estimación y prioridad.

-------------------------------------------------------------------------------
US01 — Player: Movimiento y salto
- Objetivo negocio: Permitir al jugador controlar a Kayab con respuesta inmediata y predecible.
- Criterios de aceptación:
  1) Kayab se mueve horizontalmente y salta (un solo salto); los controles responden en Desktop y Android.
  2) No hay doble salto; física Box2D aplicada; comportamiento reproducible en 5 ejecuciones.
  3) Render en Fase1 con ShapeRenderer + Box2DDebugRenderer.
- Tareas técnicas:
  - Crear/ajustar clase `Player` o `PlayerEntity` con cuerpo Box2D (BodyDef, Fixture).
  - Implementar `jump()` con `applyLinearImpulse` y `moveX` con `setLinearVelocity` preservando Y.
  - Añadir flag `onGround` usando sensor en la parte inferior y `ContactListener`.
  - Exponer métodos para testing manual (mover, saltar, obtener posición/velocidad).
- Archivos reutilizables del ejemplo:
  - `core/src/es/danirod/jddprototype/game/entities/PlayerEntity.java`
  - `core/src/es/danirod/jddprototype/box2d/BodyDefFactory.java`
  - `core/src/es/danirod/jddprototype/box2d/FixtureFactory.java`
- Estimación: 6h
- Prioridad: Must
- Dependencias: ninguna
- Prompt Copilot (plantilla):
  "Implementa US01: Crea o adapta la clase `Player` usando Box2D en `core`.
   Usa `PlayerEntity.java` del repo `jumpdontdie-master` como referencia.
   Implementa movimiento horizontal, salto único, sensor de suelo y métodos públicos para tests.
   Render en ShapeRenderer y muestra `Box2DDebugRenderer` activo. Asegúrate de documentar cómo probar manualmente." 

-------------------------------------------------------------------------------
US02 — Cámara y scroll unidireccional
- Objetivo negocio: Mantener al jugador centrado y evitar retroceso de nivel.
- Criterios de aceptación:
  1) La cámara sigue a Kayab hacia la derecha y no retrocede a la izquierda.
  2) Límite izquierdo avanza con la cámara; Kayab no puede salirse del borde visible.
  3) Suavizado (lerp) opcional configurable.
- Tareas técnicas:
  - Implementar lógica de cámara en `GameScreen` (o `CameraController`).
  - Aplicar clamp para la posición X mínima del jugador basada en la cámara.
  - Probar con niveles largos y velocidad del jugador.
- Reutilizables:
  - `core/src/es/danirod/jddprototype/game/GameScreen.java`
  - `core/src/es/danirod/jddprototype/game/Constants.java`
- Estimación: 3h
- Prioridad: Must
- Dependencias: US01
- Prompt Copilot:
  "Implementa US02: Añade cámara en `GameScreen` que sigue al player a la derecha.
   Asegura que la cámara no retroceda y que el jugador quede pegado al borde izquierdo cuando intenta moverse atrás.
   Incluye opción de smoothing y pasos para probar." 

-------------------------------------------------------------------------------
US03 — TouchControls (joystick + botón disparo)
- Objetivo negocio: Control táctil completo en Android que permite jugar sin teclado.
- Criterios de aceptación:
  1) Joystick en esquina inferior izquierda con base y knob; movimiento horizontal y salto detectados.
  2) Botón de disparo en esquina inferior derecha que dispara flecha horizontal.
  3) En Desktop, mapa a teclado (A/D, espacio, K para disparar).
- Tareas técnicas:
  - Crear `TouchControls` como `Actor` o `InputProcessor` que publique estados: moveLeft/moveRight/jump/shoot.
  - Integrar con `Player` y permitir fallback teclado.
  - Documentar tamaños/radios y cómo ajustar en `Constants`.
- Reutilizables:
  - Patrones en `MainGame.java` y `GameScreen.java`
- Estimación: 4h
- Prioridad: Must
- Dependencias: US01
- Prompt Copilot:
  "Implementa US03: Crea `TouchControls` con joystick y botón disparo.
   Exponer estados para `Player` y mapear a teclado en Desktop.
   Añade pruebas manuales y valores por defecto en `Constants`." 

-------------------------------------------------------------------------------
US04 — Box2D world y Debug render
- Objetivo negocio: Base física estable para todas las interacciones del juego.
- Criterios de aceptación:
  1) `World` creado y actualizado en `render()` con `world.step()` correcto (fixed timestep).
  2) `Box2DDebugRenderer` activo en Fase1 y `ShapeRenderer` muestra las entidades prototipo.
- Tareas técnicas:
  - Instanciar `World` y `Box2DDebugRenderer` en `GameScreen` o `Box2DScreen`.
  - Implementar time-step fijo (accumulator pattern) para `world.step()`.
  - Añadir `ShapeRenderer` para colorear entidades según TDD.
- Reutilizables:
  - `box2d/Box2DScreen.java` ejemplo del repo.
- Estimación: 4h
- Prioridad: Must
- Dependencias: US01
- Prompt Copilot:
  "Implementa US04: Crea `World` y `Box2DDebugRenderer` con paso fijo.
   Añade `ShapeRenderer` para render debug de Kayab y enemigos según los colores del TDD." 

-------------------------------------------------------------------------------
US05 — Plataformas y suelo
- Objetivo negocio: Entorno jugable estable; soporta saltos y colisiones.
- Criterios de aceptación:
  1) Plataformas estáticas colisionan correctamente con jugador.
  2) Detección de suelo permite restablecer `onGround`.
- Tareas técnicas:
  - Implementar creación de plataformas en `EntityFactory` o `Level`.
  - Definir fixtures estáticos y tamaños con `FixtureFactory`.
  - Añadir pruebas manuales de salto sobre varias plataformas.
- Reutilizables: `EntityFactory.java`, `FixtureFactory.java`
- Estimación: 5h
- Prioridad: Must
- Dependencias: US04
- Prompt Copilot:
  "Implementa US05: Crear plataformas con fixtures estáticos y sensor de suelo.
   Actualizar `EntityFactory` y documentar cómo añadir más plataformas." 

-------------------------------------------------------------------------------
US06 — Flechas (proyectiles) — jugador y enemigos
- Objetivo negocio: Mecánica central de combate; proyectiles horizontales con vida limitada.
  1) Flechas viajan horizontalmente, tienen `maxRange` y se desactivan al colisionar.
  2) Flechas jugador y enemigo distinguen `isPlayerArrow` para daño y color.
  - Implementar clase `Arrow` con body `bullet=true`, gravityScale=0.
  - Gestionar `distanceTraveled` y `active` flag; limpiar bodies al destruir.
  - Añadir spawn desde jugador y desde enemigo (intervalo).
  "Implementa US06: Clase `Arrow` con física bullet, rango máximo, y manejo de colisiones.
   Añade spawn desde `Player` y desde enemigo y ejemplos de test manual." 
 - Nota de parámetros: usar por defecto `ARROW_MAX_RANGE=200px` para flechas de jugador (~6.25m con PPM=32) y `ENEMY_ARROW_RANGE=150px` para enemigos (~4.7m). Mapear estos valores en `Constants`.

-------------------------------------------------------------------------------
US07 — Enemigo patrulla y disparo
- Objetivo negocio: Añadir reto básico en Mundo 1 y aumentar dificultad progresiva.
- Criterios de aceptación:
  1) Enemigo patrulla entre dos puntos y dispara flechas cuando el jugador está en rango.
  2) Al morir, enemigo se destruye limpiamente.
- Tareas técnicas:
  - Añadir `Enemy` con `EnemyType` y parámetros (HP, speed, fireRate).
  - Implementar patrulla con cambio de dirección al borde o X limits.
  - Integrar disparo usando `Arrow` y `ContactListener`.
- Reutilizables: `EntityFactory.java`, `PlayerEntity.java` como referencia
- Estimación: 6h
- Prioridad: Must
- Dependencias: US06
- Prompt Copilot:
  "Implementa US07: Crea `Enemy` con patrulla y disparo en rango. Usa `Arrow` para proyectiles.
   Añade parámetros configurables y ejemplos de spawn." 

-------------------------------------------------------------------------------
US08 — Salud, daño y muerte
- Objetivo negocio: Gestión de vida para jugador y enemigos; feedback claro al morir.
- Criterios de aceptación:
  1) Jugador tiene `hp` y pierde vida al recibir daño; morir ejecuta `GameOver` o respawn según reglas.
  2) Enemigos reducen HP al recibir flechas y mueren limpiamente.
- Tareas técnicas:
  - Añadir `hp` field en `Player` y `Enemy` con métodos `applyDamage(int)`.
  - Implementar UX debug (log y color change) cuando se daña.
  - Limpiar fixtures y bodies en `detach()`.
- Reutilizables: `PlayerEntity.java` (detach pattern)
- Estimación: 3h
- Prioridad: Must
- Dependencias: US07
- Prompt Copilot:
  "Implementa US08: Añade sistema de HP, applyDamage y muerte limpia en `Player` y `Enemy`.
   Documenta cómo probar daños con flechas." 

-------------------------------------------------------------------------------
US09 — Nivel 1 completo + checkpoint y guardado
- Objetivo negocio: Vertical slice jugable de Mundo 1 con guardado básico.
- Criterios de aceptación:
  1) Nivel 1 jugable: spawn, recorrido, boss o endpoint, checkpoint funcional.
  2) Guardado en SQLite al pasar checkpoint; restauración al iniciar juego.
- Tareas técnicas:
  - Crear `Level` loader (JSON simple o código) para Level 1.
  - Implementar `DatabaseManager` minimal en Android module; para Desktop usar fallback `Preferences`.
  - Guardar `current_world`, `score`, `hp` y `worlds_complete`.
- Reutilizables: `MainGame.java` (init patterns)
- Estimación: 6h
- Prioridad: Must
- Dependencias: US08
- Prompt Copilot:
  "Implementa US09: Level 1 jugable con checkpoint y guardado en SQLite.
   Implementa `DatabaseManager` en Android module y fallback para Desktop.
   Documenta flujo para crear una nueva partida y continuar." 

-------------------------------------------------------------------------------
US10 — Niveles 2–4 y dificultad incremental
- Objetivo negocio: Completar Mundo 2–4 con dificultad creciente y bosses.
- Criterios de aceptación:
  1) Cada mundo incrementa la dificultad (más enemigos, patterns, plataformas móviles).
  2) Bosses Xocotl y Vargas implementan dos fases y muestran barra de HP.
- Tareas técnicas:
  - Duplicar `Level` loader para Mundo 2–4 y ajustar parámetros de spawn y patrones.
  - Implementar Boss base con fases y barra de HP en HUD.
  - Añadir plataformas móviles y obstáculos escalables.
- Reutilizables: `Level` y `EntityFactory` patrones
- Estimación: 28h (sumatoria niveles y bosses)
- Prioridad: Must
- Dependencias: US09
- Prompt Copilot:
  "Implementa US10: Crea niveles 2, 3 y 4 con dificultad incremental y bosses Xocotl y Vargas.
   Usa parámetros para ajustar salud, velocidad y patrones. Agrega barra de HP en HUD." 

-------------------------------------------------------------------------------
US11 — HUD: vida, score y barra de boss
- Objetivo negocio: Feedback en pantalla de estado del jugador y jefes.
- Criterios de aceptación:
  1) HUD muestra vida (corazones), score y barra de HP de boss cuando aplica.
  2) Actualización en tiempo real sin afectar rendimiento.
- Tareas técnicas:
  - Crear `HUD` Actor en Scene2D con `Stage` y `Viewport` independiente.
  - Consumir datos de `Player` y `Boss` para mostrar valores.
- Reutilizables: UI patterns y `uiskin` en repo ejemplo
- Estimación: 3h
- Prioridad: Should
- Dependencias: US08, US10
- Prompt Copilot:
  "Implementa US11: HUD con vida, score y barra de boss usando Scene2D.
   Incluye ejemplos de cómo actualizar desde `GameScreen`." 

-------------------------------------------------------------------------------
US12 — Fase visual: reemplazo de sprites y animaciones
- Objetivo negocio: Mejorar apariencia sin cambiar la lógica del juego.
- Criterios de aceptación:
  1) Todas las entidades usan `SpriteBatch` y `TextureRegions` en Fase2.
  2) Las físicas y colisiones se mantienen exactamente.
- Tareas técnicas:
  - Añadir assets básicos (player, enemy, arrow, tiles) y cargar con `AssetManager`.
  - Reemplazar `renderDebug()` por `renderSprite(SpriteBatch)`.
  - Implementar animaciones simples (run/idle/shoot).
- Reutilizables: `MainGame.java` asset management
- Estimación: 8h
- Prioridad: Should
- Dependencias: US01–US11
- Prompt Copilot:
  "Implementa US12: Sustituye render debug por sprites y añade animaciones básicas.
   Asegura que los bodies Box2D no cambian y documenta cómo incluir nuevos sprites." 

-------------------------------------------------------------------------------
US13 — Integración IA para scaffolding y assets
- Objetivo negocio: Acelerar desarrollo con IA manteniendo control humano.
- Criterios de aceptación:
  1) Se usan prompts versionados para generar código/arte; cada output registrado en `.decision-log.md`.
  2) Ningún asset de IA se integra sin licencia y revisión manual.
- Tareas técnicas:
  - Definir prompts canon (code-templates, pixel-art, TMX generator).
  - Añadir `ai-sources.md` en repo con prompts y checklist de revisión.
- Estimación: 4h
- Prioridad: Must
- Dependencias: N/A
- Prompt Copilot:
  "Implementa US13: Añade carpeta `ai/` con prompts y scripts para generar scaffolding y pixel art.
   Añade `ai-sources.md` con normas de revisión y registro en `.decision-log.md`." 

-------------------------------------------------------------------------------
US14 — Tests manuales y checklist QA
- Objetivo negocio: Asegurar que cada historia cumpla criterios antes de merge.
- Criterios de aceptación:
  1) Checklist por historia en la PR describiendo pasos de prueba manual.
  2) Al menos 1 run en dispositivo exitoso para build final.
- Tareas técnicas:
  - Generar `QA_CHECKLIST.md` con pasos por historia (mover, saltar, disparar, checkpoints, bosses).
  - Integrar en plantilla de PR.
- Estimación: 3h
- Prioridad: Must
- Dependencias: US01–US12
- Prompt Copilot:
  "Implementa US14: Crea `QA_CHECKLIST.md` con pruebas manuales para cada US y añade a plantilla de PR." 

US15 — Build y despliegue de APK debug
  1) APK debug instalable y controles táctiles verificados.
  2) Incluir instrucciones de build y comandos en README.
  - Configurar `android/build.gradle` y proguard settings minimal.
  - Probar en al menos un dispositivo Android real o emulador de API 29+.
  "Implementa US15: Añade instrucciones y scripts para build de APK debug. Verifica instalación y controles en dispositivo." 

US16 — CinematicScreen (cinemáticas)
- Objetivo negocio: Presentar cinemáticas entre niveles y evento final para narrativa.
- Criterios de aceptación:
  1) Permite reproducir secuencias de cámara/imagen/texto con tiempo y callbacks para continuar el juego.
  2) Soporta texto multilínea, sprites estáticos y animaciones simples (fade/slide).
  3) Registrado en `GameState` para pausar input de jugador durante la cinemática.
- Tareas técnicas:
  - Crear `CinematicScreen` o `CinematicManager` que acepte una lista de pasos (imagen, texto, duración, callback).
  - Integrar con `GameScreen` para llamar la cinemática al completar boss o al inicio del nivel.
  - Añadir pruebas manuales: reproducir cinemática, omitir y verificar retorno al juego.
- Estimación: 4h
- Prioridad: Should
- Dependencias: US11, US12
- Prompt Copilot:
  "Implementa US16: Crea `CinematicScreen` que reproduzca secuencias de imagen y texto con callbacks.
   Añade ejemplos de cinemática final (texto + imagen) y pasos para probar manualmente."

-------------------------------------------------------------------------------
US17 — Name/Menu Screen (pantalla de nombre y menú)
- Objetivo negocio: Permitir al jugador introducir su nombre y elegir nueva partida/continuar.
- Criterios de aceptación:
  1) Pantalla principal con opciones: Nueva Partida, Continuar (si existe save), Ajustes, Salir.
  2) `NewGameScreen` permite ingresar nombre (campo de texto) y crea save inicial.
  3) Funciona en Android (teclado virtual) y Desktop (teclado físico).
- Tareas técnicas:
  - Implementar `MenuScreen` y `NewGameScreen` usando Scene2D UI (`Stage`, `Skin`, `TextField`).
  - Conectar con `DatabaseManager`/`Preferences` para crear/leer saves.
  - Documentar pasos para probar en dispositivo y emulador.
- Estimación: 3h
- Prioridad: Must
- Dependencias: US13 (para registrar saves) , US14
- Prompt Copilot:
  "Implementa US17: Crea `MenuScreen` y `NewGameScreen` con Scene2D; campo para nombre y botones Nueva/Continuar.
   Conectar con `DatabaseManager` y documentar el flujo de creación de partida."


Notas finales:
- Cada historia incluye un prompt caja-negra para Copilot; siempre pedir revisión humana antes del merge.
- Si quieres que genere los PR templates, ramas y commits para una historia concreta (ej. US01), dime cuál y lo creo con cambios scaffold en el repo de ejemplo.
