# KAYAB: HIJO DEL TRUENO
## Technical Design Document — Versión Simplificada (Demo 3 Días)
> Optimizado para generación de código con IA en Android Studio + Java + LibGDX + Box2D

---

## STACK TECNOLÓGICO

```
Motor:        LibGDX 1.12.1 (Java)
Física:       Box2D (incluido en LibGDX) — cuerpos dinámicos para jugador/enemigos,
              cuerpos estáticos para plataformas, sensores para triggers
Base de datos: SQLite (Android nativo — sin dependencias extra)
Audio:        LibGDX Sound / Music API
Resolución:   320 × 180 px (pixel art nativo) → escalado a pantalla real
FPS target:   60fps

FASE DE PROTOTIPO (Días 1-2):
  Render:     ShapeRenderer solamente — sin sprites, sin texturas
  Kayab:      rectángulo azul (16×32px)
  Enemigos:   rectángulos de color según tipo (ver abajo)
  Flechas:    rectángulos delgados 6×2px
  Plataformas: rectángulos grises
  Fondo:      Gdx.gl.glClearColor con color sólido por mundo
  Todos los hitboxes son visibles → cero bugs de posicionamiento

FASE VISUAL (Día 3):
  Reemplazar ShapeRenderer por SpriteBatch + TextureRegion
  El código de física, IA, colisiones y UI NO cambia
  Solo cambia el método render() de cada entidad
```

### Dependencias build.gradle (solo lo necesario)

```gradle
dependencies {
    implementation "com.badlogicgames.gdx:gdx:1.12.1"
    implementation "com.badlogicgames.gdx:gdx-backend-android:1.12.1"
    implementation "com.badlogicgames.gdx:gdx-tools:1.12.1"
    implementation "com.badlogicgames.gdx:gdx-box2d:1.12.1"
    implementation "com.badlogicgames.gdx:gdx-box2d-platform:1.12.1:natives-android"
    // SQLite: nativo en Android — NO necesita dependencia adicional
}
```

---

## RENDER DE PROTOTIPO (Fase 1 — ShapeRenderer)

Durante los primeros dos días, cada entidad se dibuja como una forma de color. El `Box2DDebugRenderer` ya muestra todos los hitboxes, pero además se dibujan formas de colores para identificar tipos:

```java
// Colores del prototipo
Kayab             → Color.BLUE     (rectángulo 16×32)
  + línea blanca  → indica dirección que mira (izquierda o derecha)
Guerrero Azteca   → Color.RED      (rectángulo 14×24)
Guerrero Escudo   → Color.ORANGE   (rectángulo 14×24 + rectángulo dorado al frente — solo visual)
Soldado Español   → Color.GRAY     (rectángulo 14×26)
Arcabucero        → Color.WHITE    (rectángulo 12×26)
Boss Xocotl       → Color(0.6,0,0,1) rojo oscuro (rectángulo 28×40)
Boss Vargas       → Color.DARK_GRAY (rectángulo 20×38)
Flechas jugador   → Color.YELLOW   (rectángulo 6×2)
Flechas enemigos  → Color.ORANGE   (rectángulo 6×2)

Joystick virtual  → círculo semitransparente blanco, radio 40px, esquina inferior izquierda
  + círculo interior (knob) radio 15px — se mueve con el dedo
Botón disparo     → rectángulo semitransparente blanco 70×70px, esquina inferior derecha

// Fondo por mundo (Gdx.gl.glClearColor):
Mundo 1           → 0.28, 0.56, 0.25 (verde)
Mundo 2           → 0.28, 0.45, 0.35 (verde apagado)
Mundo 3           → 0.55, 0.25, 0.05 (naranja oscuro)
Mundo 4           → 0.25, 0.25, 0.30 (gris azulado)
```

```java
// Ejemplo: render de Kayab en Fase 1
public void renderDebug(ShapeRenderer sr) {
    Vector2 pos = body.getPosition();
    float px = pos.x * PPM;
    float py = pos.y * PPM;

    sr.setColor(Color.BLUE);
    sr.rect(px - 8, py - 16, 16, 32);

    // Línea que indica dirección
    sr.setColor(Color.WHITE);
    float lineDir = facingRight ? 8 : -8;
    sr.line(px, py + 8, px + lineDir, py + 8);
}
```

**Para activar en Fase 2:** cambiar `renderDebug()` por `renderSprite(SpriteBatch batch)` en cada entidad. Nada más cambia.

---

## ARQUITECTURA MÍNIMA VIABLE

### Clases del juego

```
KayabGame.java              ← clase principal LibGDX, maneja pantallas
screens/
  MenuScreen.java           ← menú principal
  NewGameScreen.java        ← pantalla para ingresar nombre del jugador
  GameScreen.java           ← juego activo (nivel + HUD)
  CinematicScreen.java      ← muestra cinemáticas
  GameOverScreen.java       ← pantalla simple de game over
entities/
  Player.java               ← Kayab: movimiento, salto, flechas, HP
  Arrow.java                ← proyectil del jugador (solo horizontal)
  Enemy.java                ← todos los tipos de enemigo (una sola clase)
  Boss.java                 ← ambos jefes (misma clase, distintos parámetros)
world/
  Level.java                ← carga tilemap, actualiza entidades
systems/
  DatabaseManager.java      ← SQLite: crear tabla, guardar, cargar
  AudioManager.java         ← música y sfx
ui/
  HUD.java                  ← corazones, puntaje, nombre del jugador, barra de boss
  TouchControls.java        ← joystick virtual izquierdo + botón de disparo derecho
  DialogBox.java            ← caja de diálogo de cinemáticas
```

**Total: 14 clases. Una más que la versión anterior (NewGameScreen para el nombre).**

### Principio de diseño

- Una sola clase `Enemy` con un `EnemyType` enum — no crear subclases por tipo.
- Una sola clase `Boss` con parámetros configurables — no crear subclases por boss.
- Una sola clase `Level` que carga cualquier tilemap — cambiar el `.tmx` cambia el mundo.
- Una sola clase `Arrow` — solo disparo horizontal. Sin ángulo, sin diagonal.

---

## FÍSICA CON BOX2D

Box2D es el motor de física incluido en LibGDX. Se usa igual que en el ejemplo del profesor (Jump Don't Die).

### Configuración del mundo

```java
// En GameScreen.java o Level.java
World world = new World(new Vector2(0, -9.8f), true); // gravedad
Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(); // ACTIVAR en Fase 1

// En render() — Fase 1 (prototipo):
debugRenderer.render(world, camera.combined); // muestra todos los hitboxes automáticamente

// En render() — Fase 2 (visual):
// Comentar la línea de debugRenderer y dibujar sprites encima de los cuerpos Box2D
```

### Tipos de cuerpo

```java
// Kayab y enemigos: BodyDef.BodyType.DynamicBody
// Plataformas y suelo: BodyDef.BodyType.StaticBody
// Triggers (fin de nivel, spawn): BodyDef.BodyType.StaticBody + isSensor=true

BodyDef bodyDef = new BodyDef();
bodyDef.type = BodyDef.BodyType.DynamicBody;
bodyDef.position.set(startX / PPM, startY / PPM); // PPM = Pixels Per Meter = 32f
bodyDef.fixedRotation = true; // evitar que el personaje rote al chocar

Body body = world.createBody(bodyDef);
PolygonShape shape = new PolygonShape();
shape.setAsBox(8f / PPM, 16f / PPM); // mitad del ancho y alto en metros
body.createFixture(shape, 1.0f); // densidad = 1.0
shape.dispose();
```

### Movimiento del jugador con Box2D

```java
// En Player.java — mover horizontalmente desde el joystick
Vector2 vel = body.getLinearVelocity();
body.setLinearVelocity(targetVelX, vel.y); // solo modificar X, preservar Y (gravedad)

// Salto simple — solo si está en el suelo
if (joystickUp && onGround) {
    body.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), body.getWorldCenter(), true);
    onGround = false;
}
// No hay doble salto.
```

### Pared invisible izquierda (scroll bloqueado)

La cámara nunca retrocede. Si Kayab se mueve hacia la izquierda más allá del borde izquierdo visible, se saldría de pantalla. Se evita con un límite estricto en X cada frame:

```java
// En Player.java, al final de update():
// cameraLeftEdge = posición X del borde izquierdo de la cámara en metros
float cameraLeftEdge = (camera.position.x - VIRTUAL_WIDTH / 2f) / PPM;
float playerHalfWidth = 8f / PPM;

Vector2 pos = body.getPosition();
if (pos.x - playerHalfWidth < cameraLeftEdge) {
    body.setTransform(cameraLeftEdge + playerHalfWidth, pos.y, 0);
    // También anular velocidad horizontal izquierda si la tiene:
    if (body.getLinearVelocity().x < 0) {
        body.setLinearVelocity(0, body.getLinearVelocity().y);
    }
}
// Resultado: el joystick izquierdo sigue funcionando, pero Kayab
// queda "pegado" al borde visible — no desaparece de pantalla.
```

### Joystick virtual (TouchControls.java)

```java
// El joystick tiene:
//   - círculo exterior fijo (base): radio 40px, siempre visible, alpha 0.3
//   - círculo interior (knob):      radio 15px, sigue el dedo, alpha 0.6
// La posición del knob relativa al centro define la dirección:
//   knob.x - base.x > umbral (15px)  → moverse derecha
//   knob.x - base.x < -umbral        → moverse izquierda
//   knob.y - base.y > umbral         → saltar (si onGround)
// El knob no puede salir del radio del base — se clampea:

float dx = touchX - base.x;
float dy = touchY - base.y;
float dist = (float) Math.sqrt(dx*dx + dy*dy);
if (dist > BASE_RADIUS) {
    dx = dx / dist * BASE_RADIUS;
    dy = dy / dist * BASE_RADIUS;
}
knob.x = base.x + dx;
knob.y = base.y + dy;

// Estados resultantes (leídos por Player.java cada frame):
boolean moveLeft  = dx < -15;
boolean moveRight = dx > 15;
boolean jumpInput = dy > 15;   // empujar arriba en el joystick
```

### Detección de suelo (contactListener)

```java
// Sensor pequeño en la parte inferior del jugador detecta cuándo está en el suelo
world.setContactListener(new ContactListener() {
    @Override
    public void beginContact(Contact contact) {
        // Si el sensor del jugador toca una plataforma: onGround = true, jumpsUsed = 0
    }
    @Override
    public void endContact(Contact contact) {
        // Si el sensor deja de tocar: onGround = false
    }
    // preSolve y postSolve vacíos — obligatorios pero no se usan
});
```

### Constantes Box2D

```java
// Constants.java
public static final float PPM               = 32f;   // pixels per meter
public static final float PLAYER_SPEED      = 5f;    // metros/seg (= 160px/seg)
public static final float JUMP_IMPULSE      = 8f;    // metros/seg impulso (un solo salto)
public static final float ARROW_SPEED_MPS   = 15f;   // metros/seg (= 480px/seg)
public static final float ARROW_MAX_RANGE   = 6.25f; // metros (= 200px) — JUGADOR
public static final float ENEMY_ARROW_RANGE = 4.7f;  // metros (= 150px) — ENEMIGOS
public static final float ARROW_COOLDOWN    = 0.4f;  // segundos entre flechas

public static final int   VIRTUAL_WIDTH     = 320;
public static final int   VIRTUAL_HEIGHT    = 180;
public static final int   TILE_SIZE         = 16;
public static final int   PLAYER_MAX_HP     = 3;

// Puntaje
public static final int   SCORE_BASIC_ENEMY = 25;
public static final int   SCORE_SHIELD_ENEMY= 40;
public static final int   SCORE_SWORD_ENEMY = 30;
public static final int   SCORE_GUN_ENEMY   = 50;
public static final int   SCORE_WORLD_CLEAR = 500;
public static final int   SCORE_BOSS_XOCOTL = 1000;
public static final int   SCORE_BOSS_VARGAS = 2000;

// Base de datos
public static final String DB_NAME          = "kayab.db";
public static final int    DB_VERSION       = 1;
```

---

## SISTEMA DE FLECHAS (solo horizontal)

La clase `Arrow` dispara únicamente en horizontal. La dirección la define `facingRight` del jugador o enemigo. Toda flecha tiene vida útil limitada por distancia.

```java
// Arrow.java
public class Arrow {
    Body body;
    boolean active = true;
    boolean isPlayerArrow;
    float distanceTraveled = 0;
    float maxRange;     // ARROW_MAX_RANGE (jugador) o ENEMY_ARROW_RANGE (enemigos)
    Vector2 startPos;

    public Arrow(World world, float startX, float startY,
                 boolean facingRight, boolean isPlayerArrow) {
        float dir = facingRight ? 1f : -1f;

        this.maxRange = isPlayerArrow ? ARROW_MAX_RANGE : ENEMY_ARROW_RANGE;
        this.isPlayerArrow = isPlayerArrow;
        this.startPos = new Vector2(startX / PPM, startY / PPM);

        BodyDef bDef = new BodyDef();
        bDef.type = BodyDef.BodyType.DynamicBody;
        bDef.position.set(startX / PPM, startY / PPM);
        bDef.gravityScale = 0f;  // no cae por gravedad
        bDef.bullet = true;      // detección precisa para objetos rápidos
        body = world.createBody(bDef);
        body.setLinearVelocity(ARROW_SPEED_MPS * dir, 0); // solo horizontal

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(3f / PPM, 1f / PPM);
        FixtureDef fDef = new FixtureDef();
        fDef.shape = shape;
        fDef.isSensor = true;
        body.createFixture(fDef);
        shape.dispose();
        body.setUserData(this);
    }

    public void update() {
        distanceTraveled = body.getPosition().dst(startPos);
        if (distanceTraveled >= maxRange) active = false;
    }

    public void renderDebug(ShapeRenderer sr) {
        if (!active) return;
        sr.setColor(isPlayerArrow ? Color.YELLOW : Color.ORANGE);
        Vector2 pos = body.getPosition();
        sr.rect(pos.x * PPM - 3, pos.y * PPM - 1, 6, 2);
    }
}
```

### Colisión flecha–enemigo (ContactListener)

```java
// El Guerrero Escudo ya no bloquea — recibe daño igual que los demás.
// Toda colisión flecha→enemigo aplica 1 de daño y desactiva la flecha.
enemy.hp -= 1;
arrow.active = false;
```

## BASE DE DATOS — SQLite

### DatabaseManager.java

```java
// DatabaseManager.java
// Accede a SQLite nativo de Android a través de LibGDX (Gdx.app.getPreferences
// no es SQL — para SQLite real se usa la clase AndroidApplication context)

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String TABLE = "saves";

    // SQL de creación de tabla
    private static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS saves (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "player_name TEXT NOT NULL, " +
        "current_world INTEGER DEFAULT 1, " +
        "score INTEGER DEFAULT 0, " +
        "hp INTEGER DEFAULT 3, " +
        "worlds_complete TEXT DEFAULT '', " +
        "last_saved TEXT DEFAULT ''" +
        ");";

    public DatabaseManager(Context context) {
        super(context, "kayab.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Crear nueva partida (al ingresar nombre)
    public long createSave(String playerName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("player_name", playerName);
        values.put("current_world", 1);
        values.put("score", 0);
        values.put("hp", 3);
        values.put("worlds_complete", "");
        values.put("last_saved", getCurrentTimestamp());
        return db.insert(TABLE, null, values);
    }

    // Actualizar partida existente
    public void updateSave(long saveId, int world, int score, int hp, String worldsComplete) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_world", world);
        values.put("score", score);
        values.put("hp", hp);
        values.put("worlds_complete", worldsComplete);
        values.put("last_saved", getCurrentTimestamp());
        db.update(TABLE, values, "id = ?", new String[]{String.valueOf(saveId)});
    }

    // Cargar la partida más reciente
    public SaveData loadLatestSave() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null,
                                 "last_saved DESC", "1");
        if (cursor != null && cursor.moveToFirst()) {
            SaveData data = new SaveData();
            data.id             = cursor.getLong(cursor.getColumnIndex("id"));
            data.playerName     = cursor.getString(cursor.getColumnIndex("player_name"));
            data.currentWorld   = cursor.getInt(cursor.getColumnIndex("current_world"));
            data.score          = cursor.getInt(cursor.getColumnIndex("score"));
            data.hp             = cursor.getInt(cursor.getColumnIndex("hp"));
            data.worldsComplete = cursor.getString(cursor.getColumnIndex("worlds_complete"));
            data.lastSaved      = cursor.getString(cursor.getColumnIndex("last_saved"));
            cursor.close();
            return data;
        }
        return null; // no hay partida guardada
    }

    // Verificar si existe alguna partida guardada
    public boolean hasSave() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
               java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
```

### SaveData.java (POJO simple)

```java
// SaveData.java
public class SaveData {
    public long   id;
    public String playerName;
    public int    currentWorld;
    public int    score;
    public int    hp;
    public String worldsComplete;  // "1,2,3" — mundos terminados
    public String lastSaved;
}
```

### Cómo usar DatabaseManager desde GameScreen

```java
// En GameScreen.java
// Inicialización (recibir el manager desde KayabGame):
DatabaseManager db = game.getDatabaseManager();

// Al terminar un mundo:
saveData.score += Constants.SCORE_WORLD_CLEAR;
saveData.currentWorld = nextWorld;
saveData.hp = player.hp;
saveData.worldsComplete += "," + completedWorld;
db.updateSave(saveData.id, saveData.currentWorld,
              saveData.score, saveData.hp, saveData.worldsComplete);

// Al derrotar un boss:
saveData.score += Constants.SCORE_BOSS_XOCOTL; // o SCORE_BOSS_VARGAS
db.updateSave(...);
```

### Pasar el Context de Android a LibGDX

SQLite requiere un `Context` de Android. La forma correcta en LibGDX:

```java
// En AndroidLauncher.java (ya existe en el proyecto):
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    KayabGame game = new KayabGame();
    game.setDatabaseManager(new DatabaseManager(this)); // pasar el context aquí
    initialize(game, config);
}

// En KayabGame.java:
private DatabaseManager databaseManager;
public void setDatabaseManager(DatabaseManager db) { this.databaseManager = db; }
public DatabaseManager getDatabaseManager() { return databaseManager; }
```

---

## SISTEMA DE ENEMIGOS (una sola clase)

```java
// Enemy.java
public enum EnemyType {
    AZTEC_BASIC,    // Mundos 1, 2, 3
    AZTEC_SHIELD,   // Mundos 1, 2, 3
    SPANISH_SWORD,  // Mundo 4
    SPANISH_GUN     // Mundo 4
}

public class Enemy {
    EnemyType type;
    float x, y, velocityX = 0, velocityY = 0;
    int hp;
    boolean onGround, facingRight = true;
    float attackTimer = 0;
    float patrolLeft, patrolRight;  // límites de patrulla

    // Configuración por tipo (asignada en constructor):
    float detectionRange;
    float attackCooldown;
    float moveSpeed;

    // Puntos al morir — según tipo:
    public int getScoreValue() {
        switch (type) {
            case AZTEC_BASIC:   return Constants.SCORE_BASIC_ENEMY;
            case AZTEC_SHIELD:  return Constants.SCORE_SHIELD_ENEMY;
            case SPANISH_SWORD: return Constants.SCORE_SWORD_ENEMY;
            case SPANISH_GUN:   return Constants.SCORE_GUN_ENEMY;
        }
        return 0;
    }
}
```

---

## SISTEMA DE JEFES (una sola clase)

```java
// Boss.java
public class Boss {
    String id;           // "xocotl" o "vargas"
    float x, y;
    int maxHp, currentHp;
    int phase = 1;
    float attackTimer = 0;
    boolean facingRight = true;

    // Parámetros configurables:
    float attackCooldown;
    float moveSpeed;
    boolean hasShockwave;    // Xocotl: onda de choque en suelo
    boolean hasGun;          // Vargas: pistola con telegrafía
    float telegraphTime = 3f; // solo SPANISH_GUN y Vargas

    // Cambio de fase al 50% HP:
    public void checkPhase() {
        if (currentHp <= maxHp / 2 && phase == 1) {
            phase = 2;
            attackCooldown *= 0.75f; // más agresivo
            // activar shake de cámara
        }
    }

    // Puntos al morir:
    public int getScoreValue() {
        return id.equals("vargas")
            ? Constants.SCORE_BOSS_VARGAS
            : Constants.SCORE_BOSS_XOCOTL;
    }
}
```

---

## CONTROLES TÁCTILES — JOYSTICK + 1 BOTÓN

```
LAYOUT EN PANTALLA:
┌─────────────────────────────────────────────┐
│                                             │
│  [JOYSTICK]                   [◉ DISPARO]   │
│  ← → arriba=saltar                          │
│                                             │
└─────────────────────────────────────────────┘
```

```java
// TouchControls.java
// Joystick virtual izquierdo + un botón de disparo derecho
// Sin botones separados de dirección. Sin botón diagonal.

// En Player.update():
if (controls.firePressed) shootHorizontal(); // solo horizontal, sin diagonal
```

---

## HUD

```java
// HUD.java
// Dibuja en coordenadas de pantalla (no del mundo):
// ♥♥♥           → esquina superior izquierda (vida de Kayab)
// NOMBRE: Kayab → debajo de los corazones
// Score: 1250   → esquina superior derecha
// [████░░] BOSS → barra de HP del boss, solo durante pelea de boss
//                 centrada en la parte superior
```

---

## SISTEMA DE NIVELES (reutilizable)

### Tilemap base

```
Resolución de juego:  320 × 180 px
Tamaño de tile:       16 × 16 px
Longitud nivel:       100–160 tiles de ancho (scroll horizontal)
Arena de boss:        30–40 tiles de ancho (fija, sin scroll)
```

### Capas del tilemap

```
BACKGROUND   → decoración de fondo, sin colisión, parallax a 0.3x
PLATFORMS    → plataformas sólidas, con colisión
OBJECTS      → posición de enemigos y spawn de Kayab (objetos de Tiled)
```

### Tipos de plataforma

```
SOLID    → colisión total. Rectángulo estático.
ONE_WAY  → solo colisión desde arriba.
MOVING   → se mueve entre punto A y B. Solo en Mundos 2, 3, 4.
HUECO    → ausencia de suelo entre plataformas. No es un objeto — es simplemente vacío.
```

### Muerte por hueco (pit death)

Si `body.getPosition().y * PPM < -50` → muerte instantánea, sin importar los corazones que tenga Kayab.

```java
// En Player.java, dentro de update():
if (body.getPosition().y * PPM < -50f) {
    die(); // igual que llegar a 0 HP → Game Over → reintentar nivel
}
```

Los enemigos **no pueden caer a huecos**. En lugar de raycasts o sensores dinámicos (costosos de depurar), se usa un objeto invisible en el tilemap:

**Método: Objeto "Inversor_Enemigo" en Tiled**

En la capa `OBJECTS` del `.tmx`, colocar objetos rectangulares delgados (`type = "inversor"`) en el borde de cada plataforma donde hay hueco. Cuando el cuerpo Box2D del enemigo toca ese sensor, invierte su dirección.

```java
// En Level.java — al cargar la capa OBJECTS del .tmx:
// Crear cuerpos estáticos isSensor=true para cada objeto tipo "inversor"

// En ContactListener — beginContact:
if (isEnemyBody(bodyA) && isInversorSensor(bodyB)) {
    ((Enemy) bodyA.getUserData()).facingRight =
        !((Enemy) bodyA.getUserData()).facingRight;
}

// Ventaja: cero física compleja. El diseñador coloca los inversores
// en Tiled una vez y funcionan para todos los enemigos del nivel.
```

En el prototipo (Fase 1), los huecos son visibles automáticamente porque el fondo es de color sólido y las plataformas son rectángulos grises — el vacío entre plataformas se ve claramente.

### Reutilización entre mundos

El código de `Level.java` nunca cambia. Para cada mundo, cambiar solo:
1. El archivo `.tmx` (misma estructura, distintas plataformas)
2. El tileset PNG (misma grid, distintos gráficos)
3. El color overlay (rectángulo semitransparente de mood)
4. La lista de enemigos a spawnear (leída desde la capa OBJECTS del `.tmx`)
5. La música

---

## CINEMÁTICAS (SISTEMA SIMPLE)

```java
// CinematicScreen.java
public class DialogScene {
    String backgroundImage;   // nombre del asset de fondo
    String characterLeft;     // sprite izquierdo (null si no hay)
    String characterRight;    // sprite derecho (null si no hay)
    String speakerName;
    Color  speakerColor;
    String text;
}

// El jugador toca la pantalla para avanzar.
// Sin JSON. Sin parser. Solo un array de objetos Java.
```

---

## EFECTOS VISUALES (todos fakeados)

### Parallax

```java
float bgX = -camera.position.x * 0.3f;
batch.draw(backgroundFar, bgX, 0);
float mgX = -camera.position.x * 0.6f;
batch.draw(backgroundMid, mgX, 0);
```

### Shake de cámara

```java
float shakeTime = 0;
// Activar: shakeTime = 0.3f;
// En render():
if (shakeTime > 0) {
    shakeTime -= deltaTime;
    camera.position.x += MathUtils.random(-3f, 3f);
    camera.position.y += MathUtils.random(-3f, 3f);
}
```

### Lluvia (Mundo 4)

```java
// Array de 30 líneas con posición y velocidad aleatoria
// Cada frame: mover hacia abajo + resetear al salir de pantalla
// Dibujar con ShapeRenderer como líneas blancas con alpha 0.4
```

### Partículas al morir enemigo

```java
// Al morir un enemigo: 5–8 objetos con velocidad aleatoria y vida 0.3–0.5s
// No es un sistema de partículas real — solo un ArrayList de puntos simples
```

---

## ESTRUCTURA DE CARPETAS

```
app/src/main/
├── java/com/kayab/
│   ├── KayabGame.java
│   ├── screens/
│   │   ├── MenuScreen.java
│   │   ├── NewGameScreen.java      ← NUEVO: ingresar nombre
│   │   ├── GameScreen.java
│   │   ├── CinematicScreen.java
│   │   └── GameOverScreen.java
│   ├── entities/
│   │   ├── Player.java
│   │   ├── Arrow.java              ← solo disparo horizontal
│   │   ├── Enemy.java
│   │   └── Boss.java
│   ├── world/
│   │   └── Level.java
│   ├── systems/
│   │   ├── DatabaseManager.java   ← NUEVO: SQLite
│   │   └── AudioManager.java
│   └── ui/
│       ├── HUD.java
│       ├── TouchControls.java      ← joystick virtual izquierdo + botón de disparo derecho
│       └── DialogBox.java
└── assets/
    ├── maps/
    │   ├── world1.tmx
    │   ├── world2.tmx
    │   ├── world3.tmx
    │   └── world4.tmx
    ├── sprites/
    │   ├── kayab_sheet.png
    │   ├── enemies_sheet.png
    │   ├── bosses_sheet.png
    │   ├── tiles_world1.png
    │   ├── tiles_world2.png
    │   ├── tiles_world3.png
    │   ├── tiles_world4.png
    │   └── ui/
    ├── cinematics/
    │   ├── backgrounds/
    │   └── characters/
    └── audio/
        ├── music/
        └── sfx/
```

---

## PALETAS DE COLOR

```
MUNDO 1 (cálida):
  Verde selva:  #4A8F3F
  Ocre dorado:  #D4A843
  Azul cielo:   #6BBDE3
  Overlay:      ninguno

MUNDO 2 (cálida + fría):
  Hereda Mundo 1 + gris metálico #8A9A9A
  Overlay: rgba(100,100,120, 0.08)

MUNDO 3 (dorado y caos):
  Dorado: #FFD700 | Rojo: #8B0000
  Overlay: rgba(180,80,0, 0.1) + humo rgba(40,40,40, 0.15)

MUNDO 4 (apagado):
  Paleta del Mundo 1 desaturada al 40%
  Overlay: rgba(60,70,90, 0.25)
  Kayab: mantiene su paleta original sin modificar
```

---

## PLAN DE DESARROLLO — 3 DÍAS

### Día 1 (fundación + física)

```
Mañana:
  [ ] Proyecto Android Studio + LibGDX + Box2D configurado
  [ ] KayabGame.java + pantallas base (Menu, GameOver, Cinematic)
  [ ] DatabaseManager.java — crear tabla, createSave, updateSave, loadLatestSave
  [ ] NewGameScreen.java — campo de texto para nombre
  [ ] Constants.java — todas las constantes Box2D y de juego

Tarde:
  [ ] Player.java con Box2D: movimiento, un solo salto, ContactListener para suelo
  [ ] Arrow.java — disparo horizontal con distanceTraveled + maxRange
  [ ] TouchControls.java — joystick virtual izquierdo + botón disparo derecho
  [ ] Level.java cargando un .tmx simple con Box2D para plataformas
  [ ] RENDER PROTOTIPO: todo con ShapeRenderer + Box2DDebugRenderer
```

### Día 2 (contenido — los 4 mundos)

```
Mañana:
  [ ] Enemy.java con EnemyType enum — AZTEC_BASIC funcionando
  [ ] IA de enemigos: patrulla + detección de rango + disparo
  [ ] Flechas enemigas con ENEMY_ARROW_RANGE (150px) — desaparecen solas
  [ ] AZTEC_SHIELD — más HP (3 en vez de 2), avanza sin detenerse hacia Kayab
  [ ] HUD.java — corazones, puntaje, nombre, barra de boss
  [ ] Mundo 1 jugable de inicio a fin con prototipo visual

Tarde:
  [ ] Boss.java — Xocotl (Mundo 3) y Vargas (Mundo 4)
  [ ] CinematicScreen.java con DialogBox (array Java, sin JSON)
  [ ] Mundos 2, 3 y 4 jugables (reusar Level.java, cambiar .tmx y fondo)
  [ ] Integración SQLite: guardar al terminar cada mundo
  [ ] Pantalla Continuar: cargar desde SQLite
  [ ] Flujo completo: Menú → Cinemática → Mundo → Boss → Cinemática → siguiente
```

### Día 3 (gráficos + polish)

```
Mañana:
  [ ] Sprites de Kayab — reemplazar ShapeRenderer por SpriteBatch
  [ ] Sprites de enemigos y bosses
  [ ] Tileset Mundo 1 y Mundo 4 (los más importantes narrativamente)
  [ ] Tilesets Mundo 2 y Mundo 3
  [ ] Backgrounds con parallax (2 capas)

Tarde:
  [ ] AudioManager.java + música y sfx
  [ ] Efectos visuales: shake de cámara, overlay de color por mundo
  [ ] Lluvia en Mundo 4 (30 líneas diagonales animadas)
  [ ] Humo en Mundo 3 (rectángulos semitransparentes)
  [ ] Cinemáticas con imágenes de fondo reales
  [ ] Pruebas en dispositivo Android — ajuste de velocidades y dificultad
  [ ] Build final APK
```

---

## NOTAS PARA LA IA AL GENERAR CÓDIGO

Al pedirle a una IA que genere código para este juego:

1. **Física:** "Box2D incluido en LibGDX. PPM=32f. Cuerpos dinámicos para jugador/enemigos, estáticos para plataformas."
2. **Prototipo:** "Fase 1: todo con ShapeRenderer y Box2DDebugRenderer. Sin texturas. El método renderDebug() se reemplazará por renderSprite() en Fase 2."
3. **Dirección:** "Kayab avanza de izquierda a derecha. La cámara no retrocede. El nivel tiene un X_MAX definido."
4. **Duración:** "Cada nivel dura 4-5 minutos. Diseñar cantidad de enemigos y longitud del mapa en consecuencia."
5. **Controles:** "Joystick virtual izquierdo (←→ + arriba=saltar). Botón derecho = disparar. Solo 2 elementos de UI táctil."
6. **Salto:** "Un solo salto. Sin doble salto. onGround se detecta con ContactListener en Box2D."
7. **Flechas:** "Solo horizontal. Arrow recibe (world, x, y, facingRight, isPlayerArrow). Sin ArrowType enum — ya no hay diagonal."
8. **Rango flechas:** "maxRange = ARROW_MAX_RANGE (200px = 6.25m) para jugador, ENEMY_ARROW_RANGE (150px = 4.7m) para enemigos. Ambas desaparecen al superar el rango."
9. **Escudo:** "El Guerrero Escudo ya NO bloquea. Solo tiene más HP (3 en vez de 2). Avanza hacia Kayab sin detenerse."
10. **Enemigos:** "Una sola clase Enemy con EnemyType enum — no crear subclases."
11. **Base de datos:** "SQLite nativo de Android. DatabaseManager extiende SQLiteOpenHelper. Context desde AndroidLauncher."
12. **Guardado:** "Solo al terminar un mundo o boss. No guardar mid-nivel."
13. **Cinemáticas:** "Array de objetos DialogScene en Java. Sin JSON. Sin parser."
14. **ContactListener:** "Toda la lógica de colisión va en el ContactListener del mundo Box2D."
15. **Pared izquierda:** "Cada frame, clampear la posición X de Kayab para que no quede a la izquierda de (camera.position.x - VIRTUAL_WIDTH/2) / PPM. Anular también la velocidad horizontal negativa si supera ese límite."
16. **Inversores de enemigos:** "Los objetos type='inversor' en la capa OBJECTS del .tmx se cargan como sensores estáticos Box2D. Al contacto con un enemigo, invertir su facingRight. Sin raycasts ni sensores dinámicos."

### Qué fakeear sin dudarlo

- Fuego: sprite animado de 4–6 frames
- Humo: rectángulos semitransparentes con alpha oscilante (ShapeRenderer en Fase 1)
- Lluvia: array de 30 líneas diagonales con posición Y aleatoria (ShapeRenderer en Fase 1)
- Iluminación: overlay de color con alpha
- Parallax: mover el fondo a fracción de la velocidad de cámara
- Partículas de muerte: 5-8 puntos pequeños con velocidad aleatoria (ShapeRenderer en Fase 1)

### Qué evitar para no generar bugs

- No mezclar coordenadas de pixels con coordenadas de Box2D (siempre dividir/multiplicar por PPM)
- No crear cuerpos Box2D en el hilo de renderizado — crearlos antes o en el step del mundo
- No llamar world.step() más de una vez por frame
- No pedir sistemas desacoplados complejos (EventBus, ECS, ServiceLocator)
- No usar herencia profunda — preferir composición simple
- No abrir SQLite desde el hilo de LibGDX — solo desde AndroidLauncher o AsyncTask
