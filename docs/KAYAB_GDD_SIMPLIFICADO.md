# KAYAB: HIJO DEL TRUENO
## Game Design Document — Versión Simplificada (Demo 3 Días)

---

## DATOS GENERALES

| Campo | Valor |
|---|---|
| Nombre | Kayab: Hijo del Trueno |
| Género | Plataformas 2D arcade |
| Motor | Android Studio + Java + LibGDX + Box2D |
| Vista | 2D lateral scrolling |
| Jugadores | 1 jugador |
| Plataforma | Android |
| Alcance | Demo cinematográfico — 4 mundos, 1 nivel por mundo |

---

## CONCEPTO CENTRAL

Un demo corto, visualmente poderoso y narrativamente impactante.
El objetivo no es un juego completo. Es que parezca uno.

**Fórmula:** Cinemática breve → nivel de 4-5 minutos → cinemática breve → repite × 4.

### Enfoque de desarrollo: Prototipo primero

**Fase 1 (Días 1-2):** Todo el juego se construye con formas geométricas simples:
- Kayab = rectángulo azul con una línea indicando hacia dónde mira
- Enemigos = rectángulos de colores (rojo=azteca básico, naranja=escudo, gris=español espada, blanco=arcabucero)
- Bosses = rectángulo grande con barra de HP visible encima
- Flechas = líneas o rectángulos delgados
- Plataformas = rectángulos grises/marrones
- Fondo = color sólido por mundo (verde, verde+gris, naranja oscuro, gris)

**Ventaja:** cero bugs visuales, hitboxes perfectamente visibles, fácil depurar.

**Fase 2 (Día 3):** Reemplazar cada forma con su sprite de pixel art. El código de física, IA y colisiones NO cambia. Solo cambia cómo se dibuja cada entidad.

### Dirección del juego

**Kayab siempre avanza de izquierda a derecha.** El nivel empieza en X=0 (spawn de Kayab) y termina en X=MAX (entrada al área de boss). La cámara sigue a Kayab y nunca retrocede. Los enemigos patrullan en su segmento pero Kayab no puede ir hacia la izquierda más allá de lo que la cámara ya mostró.

### Duración por capítulo

Cada capítulo debe durar entre 4 y 5 minutos de juego activo:
- Sección de plataformas con enemigos: ~3 minutos (scroll horizontal)
- Pelea de boss: ~1.5 minutos
- Los niveles se diseñan con esta duración en mente — ni muy cortos ni muy largos.

---

## HISTORIA (LINEAL, SIN RAMIFICACIONES)

Kayab es un guerrero del pueblo Chimalpa que se alía con los españoles para derrotar al Imperio Azteca. Destruye a Xocotl, el gran capitán azteca. Cree haber ganado. En el Mundo 4 descubre que fue traicionado y su gente serà esclava de los españoles. Aunque derrota al Capitán Vargas, el final es oscuro: la victoria militar no cambia el papel que lo esclaviza.

**No hay decisiones. No hay finales alternativos. La historia es una sola.**

### Arco por mundo

| Mundo | Subtítulo | Resumen narrativo |
|---|---|---|
| 1 | La Tierra del Trueno | Kayab defiende su aldea de los aztecas solo. Aprende que necesita aliados. |
| 2 | El Pacto de Hierro | Primera alianza con España. Kayab empieza a ver señales de alarma. |
| 3 | El Corazón del Imperio | La gran invasión. Kayab derrota a Xocotl. Cree que ganó. |
| 4 | La Traición | Descubre que su gente serà esclavizada. Derrota a Vargas pero igual no puede evitar el dominio español de la region. |

### Final único

Kayab derrota a Vargas. Lo tiene a su merced. No lo mata. Le dice:

> *"Gané todas las batallas pero mi pueblo salió perdiendo."*

**Epílogo (texto en pantalla):**
> *"Kayab y su pueblo resistieron cuarenta años más. No aparecen en los libros españoles. Pero los Chimalpa los recuerdan."*
>
> *"Esta historia está basada en hechos reales. Los Tlaxcaltecas y miles de pueblos indígenas vivieron exactamente esto."*

---

## PERSONAJES

### Kayab (protagonista)
- Guerrero joven, arquero experto
- Arco como única arma durante todo el juego
- Sprite: pixel art cálido, plumas en el cabello, taparrabos de guerrero, 16×32 px

### Citlali (abuela, NPC en cinemáticas)
- Aparece solo en cinemáticas de apertura y cierre
- Frase clave: *"Ten cuidado con lo que invitas, nieto."*

### Capitán Vargas (español)
- Aparece en cinemática del Mundo 2 como aliado
- Jefe final del Mundo 4
- Sprite: paleta fría, armadura de acero

### Malinalli (intérprete, solo cinemáticas)
- Aparece en Mundo 2
- Línea clave: *"Yo también le creí a alguien así, una vez."*

### Xocotl (azteca — Jefe del Mundo 3)
- Capitán de la Guardia Real azteca
- Reaparece en cinemática del Mundo 4 como aliado silencioso (no jugable)

---

## GAMEPLAY — MECÁNICAS COMPLETAS

```
CONTROLES:
  JOYSTICK IZQUIERDO (virtual, transparente):
    ← →  : mover izquierda / derecha
    ↑    : saltar (un solo salto — sin doble salto)
    El joystick no afecta la dirección del disparo.

  BOTÓN DERECHO (único botón de acción):
    ◉   : dispara una flecha horizontal en la dirección que mira Kayab

  La flecha siempre es horizontal. No hay diagonal.
  La dirección del disparo la define hacia dónde mira Kayab (última dirección de movimiento).

CICLO DE VIDA DE LAS FLECHAS (jugador y enemigos):
  - Cada flecha viaja horizontalmente hasta 200px (jugador) o 150px (enemigos).
  - Al superar esa distancia, la flecha desaparece aunque no haya golpeado nada.
  - Una flecha desaparece también al impactar con cualquier plataforma sólida o con el jugador/enemigo.

VIDA:
  3 corazones
  Al llegar a 0 → Game Over → reintentar nivel
  Caer a un hueco → muerte instantánea, independientemente de los corazones restantes → reintentar nivel
```

**Progresión por mundo:**
- Mundo 1: tutorial — correr, saltar, disparar horizontal. Enemigos indígenas del Imperio en pequeña cantidad. Es casi imposible perder.
- Mundo 2: Enemigos del imperio. Dificultad mayor. Plataformas móviles.
- Mundo 3: Enemigos ranged. Boss Xocotl.
- Mundo 4: Enemigos españoles. Boss Vargas. Final narrativo.

> **Nota de prototipo:** En Fase 1, los controles táctiles muestran un círculo semitransparente a la izquierda (joystick virtual) y un rectángulo semitransparente a la derecha etiquetado `DISPARO`. Ambos son completamente visibles para facilitar las pruebas.

### Por qué el diseño de un solo disparo funciona

Con solo flecha horizontal, el diseño de niveles y enemigos se adapta así:

1. El **Guerrero Escudo** ya no bloquea desde el frente — se elimina esa mecánica. En su lugar, el Guerrero Escudo tiene más HP (3 flechas) y avanza lentamente: el reto es mantener distancia y disparar antes de que llegue.
2. El **Boss Xocotl** ya no tiene debilidad de disparo diagonal. Su debilidad es la ventana de tiempo entre sus ataques: cuando termina de lanzar una onda de choque, está quieto ~1.5 segundos — esa es la ventana para disparar.
3. Los **Arcabuceros** ya no están en plataformas elevadas inalcanzables. Se colocan al mismo nivel pero detrás de obstáculos — el jugador debe avanzar, esquivar el disparo telegráfico, y acercarse lo suficiente para que su flecha llegue (recordar: rango máximo 200px).
4. El **salto** sigue siendo útil para esquivar flechas y ondas de choque — el reto es el timing, no el ángulo de disparo.

---

## SISTEMA DE PUNTAJE Y PROGRESO (SQLite)

### Datos guardados por partida

La base de datos SQLite tiene una tabla `saves` con las siguientes columnas:

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | ID único de la partida |
| `player_name` | TEXT | Nombre ingresado por el jugador al crear la partida |
| `current_world` | INTEGER | Mundo donde quedó el jugador (1–4) |
| `score` | INTEGER | Puntaje acumulado total |
| `hp` | INTEGER | Vida al momento del último guardado (1–3) |
| `worlds_complete` | TEXT | Mundos terminados separados por coma. Ej: "1,2,3" |
| `last_saved` | TEXT | Fecha y hora del último guardado (ISO 8601) |

### Cuándo se guarda

- Al completar un mundo (automático)
- Al derrotar un boss (automático)
- Al terminar el juego (automático)
- No hay guardado manual dentro del nivel — si muere, reinicia el nivel desde el inicio pero conserva el puntaje acumulado hasta ese mundo.

### Cómo se acumula el puntaje

| Acción | Puntos |
|---|---|
| Derrotar Guerrero Azteca Básico | +25 |
| Derrotar Guerrero Azteca Escudo | +40 |
| Derrotar Soldado Español Espada | +30 |
| Derrotar Arcabucero Español | +50 |
| Completar un mundo (llegar al boss y ganarlo) | +500 |
| Derrotar Boss Xocotl | +1000 |
| Derrotar Boss Vargas (final del juego) | +2000 |

El puntaje se muestra en el HUD durante el juego (esquina superior derecha).

### Pantalla de nombre del jugador

Al iniciar una partida nueva aparece una pantalla con:
- Campo de texto: *"Ingresa tu nombre, guerrero"*
- Botón confirmar → crea un nuevo registro en SQLite y empieza el juego

### Pantalla Continuar

Al elegir "Continuar" en el menú:
- Se carga el registro con el `id` más reciente de SQLite
- El jugador retoma desde el inicio del `current_world` guardado
- Con el `hp` y el `score` guardados

---

## ENEMIGOS

### Tipo 1: Guerrero Azteca Básico (Mundos 1, 2, 3)
- HP: 2 flechas
- Patrulla de izquierda a derecha en plataforma
- Si Kayab entra en rango de 200px: se detiene y dispara una flecha cada 2 segundos
- Color: tonos tierra, rojo sangre, negro

### Tipo 2: Guerrero Azteca Escudo (Mundos 1, 2, 3)
- HP: 3 flechas (más resistente que el básico)
- **No bloquea flechas** — la mecánica de escudo se eliminó junto con el disparo diagonal
- Avanza lentamente hacia Kayab sin detenerse — el reto es mantener distancia y dispararle antes de que llegue
- Color: tonos tierra + rectángulo dorado visible al frente (indicador visual de su mayor resistencia)

### Tipo 3: Soldado Español Espada (Mundo 4)
- HP: 2 flechas
- Carga hacia Kayab cuando entra en rango de 150px
- Ataque melee que hace 1 daño
- Sin ataque a distancia — mantener distancia es suficiente

### Tipo 4: Arcabucero Español (Mundo 4)
- HP: 2 flechas
- Colocado al mismo nivel que Kayab, pero detrás de obstáculos o columnas
- Telegrafía el disparo: 3 segundos de animación de "apuntar" visible antes de disparar
- Disparo horizontal que cruza hasta 150px (igual que cualquier flecha enemiga)
- El más peligroso porque su disparo telegráfico es largo y fácil de ignorar — hay que acercarse con cuidado

**Regla de IA de todos los enemigos:** Patrullan y atacan solo en su eje horizontal. No persiguen por plataformas. **Nunca se acercan al borde de un hueco** — al detectar el borde invierten dirección automáticamente.

---

## JEFES

Ambos jefes usan el mismo sistema base. Solo cambian sprite, velocidades y patrón de ataque.

### Boss Mundo 3: Xocotl
- Plataforma de arena fija (sin scroll)
- Fase 1: Lanza ondas de choque al suelo con su macana — Kayab debe saltar para esquivarlas
- Fase 2 (al 50% HP): Salta a la plataforma donde está Kayab + sigue con ondas
- **Ventana de ataque:** ~1.5 segundos de quietud después de cada onda de choque — disparar en ese momento
- Al "caer": cinemática breve — Kayab lo deja vivir

### Boss Mundo 4: Vargas
- Fase 1: Disparo de pistola que cruza toda la pantalla (3 seg de recarga visible)
- Fase 2 (al 50% HP): Pistola + espada en rango corto
- Ventana de ataque: durante los 3 segundos de recarga de la pistola
- Al "caer": cinemática final — no muere, termina el juego

**Ambos jefes: 2 fases, barra de HP visible en la parte superior de la pantalla.**

---

## MUNDOS — VISUAL Y AMBIENTE

Cada mundo reutiliza el mismo sistema de niveles. Solo cambian tileset, paleta, música y enemigos.

### Mundo 1: La Tierra del Trueno
- **Paleta:** Verde selva, ocre dorado, azul cielo claro
- **Fondo:** Montañas, árboles, aldea Chimalpa
- **Música:** Gaita lenta, tambores suaves
- **Efecto especial:** Ninguno (tutorial limpio)

### Mundo 2: El Pacto de Hierro
- **Paleta:** Verde del Mundo 1 mezclado con grises metálicos
- **Fondo:** Campamentos españoles, selva con estandartes
- **Música:** Gaita más urgente, primeros sonidos de metal
- **Efecto especial:** Plataformas móviles (horizontal y vertical)

### Mundo 3: El Corazón del Imperio
- **Paleta:** Dorado intenso, rojo guerra, humo negro
- **Fondo:** Tenochtitlan ardiendo, pirámides, lago
- **Música:** Tambores aztecas en tensión, gaita urgente
- **Efecto especial:** Overlay de humo falso semitransparente, partículas de chispa

### Mundo 4: La Traición
- **Paleta:** Los colores del Mundo 1 desaturados al 40%. Gris dominante.
- **Kayab:** Mantiene su paleta original — contraste visual intencional.
- **Fondo:** La aldea del Mundo 1 transformada — cruces donde había templos
- **Música:** Fría y minimalista → recupera el tema del Mundo 1 en el nivel final
- **Efecto especial:** Lluvia falsa (líneas diagonales animadas, overlay semitransparente)

---

## CINEMÁTICAS

### Formato
Imagen estática en pixel art + caja de diálogo inferior + avanzar tocando pantalla. Sin animaciones complejas.

### Lista de cinemáticas

| ID | Cuándo | Pantallas aprox. |
|---|---|---|
| intro | Al inicio del juego | 4–5 |
| world1_open | Antes del Mundo 1 | 3–4 |
| world1_close | Después del Mundo 1 | 2–3 |
| world2_open | Antes del Mundo 2 | 3–4 |
| world2_close | Después del Mundo 2 | 2–3 |
| world3_open | Antes del Mundo 3 | 2–3 |
| world3_close | Después de derrotar a Xocotl | 2–3 |
| world4_open | Antes del Mundo 4 (el documento) | 3–4 |
| final | Después de derrotar a Vargas | 3–4 |
| epilogo | Texto de cierre | 2 (solo texto) |

---

## EFECTOS VISUALES FALSOS (fakeados)

| Efecto | Implementación |
|---|---|
| Lluvia | Array de 30 líneas blancas diagonales animadas (Mundo 4) |
| Humo | Rectángulos semitransparentes con alpha oscilante (Mundo 3) |
| Fuego | Sprite animado de 4–6 frames en loop |
| Partículas | 5–10 puntos pequeños con velocidad aleatoria al morir enemigo |
| Shake de cámara | Offset aleatorio de ±3px en x/y durante 0.3 segundos |
| Overlay de color | Rectángulo del tamaño de la pantalla con color y alpha bajo por mundo |
| Parallax | 2 capas de fondo a 0.3x y 0.6x la velocidad del jugador |

---

## FLUJO DE PANTALLAS

```
SPLASH (logo)
    ↓
MENÚ PRINCIPAL
    ├── Nueva Partida → Ingresar nombre → Guardar en SQLite → Intro → Mundo 1
    ├── Continuar     → Sleccionar registro deseado
    └── Opciones      → volumen música / efectos

POR CADA MUNDO:
  CINEMÁTICA APERTURA
      ↓
  NIVEL (HUD: ♥♥♥ | puntaje | nombre del jugador)
      ↓ (si HP = 0 → Game Over: reintentar / menú)
  BOSS
      ↓ (al ganar: +1000 pts, guardar en SQLite)
  CINEMÁTICA CIERRE
      ↓
  (siguiente mundo)

AL TERMINAR EL JUEGO:
  Cinemática final → Epílogo → Créditos → Menú principal
```

---

## GLOSARIO (menú del juego)

| Término | Definición |
|---|---|
| Chimalpa | El pueblo de Kayab. De "chimalli" — escudo en náhuatl. |
| Náhuatl | Lengua azteca, hablada por más de un millón de personas hoy. |
| Tlatoani | Líder supremo azteca. |
| Tenochtitlan | Capital azteca. Hoy Ciudad de México. |
| Arcabuz | Arma de fuego española de la conquista. Lenta, devastadora. |
| Encomienda | Sistema colonial: indígenas asignados a trabajar para españoles. |
| Malinalli | La Malinche — intérprete real de Cortés. Figura histórica compleja. |
| Tlaxcaltecas | Pueblo real que se alió con España y fue traicionado. |
