package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.entities.Arrow;
import com.kayab.entities.Boss;
import com.kayab.entities.Enemy;
import com.kayab.entities.EntityFactory;
import com.kayab.entities.Player;
import com.kayab.gfx.Background;
import com.kayab.gfx.GameArt;
import com.kayab.persistence.SaveData;
import com.kayab.ui.Hud;
import com.kayab.ui.TouchControls;

public class GameScreen implements Screen {

    private final KayabGame game;
    private final SaveData saveData;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private static final float FIXED_STEP   = 1f / 60f;
    private static final int   VELOCITY_IT  = 6;
    private static final int   POSITION_IT  = 2;
    private float accumulator = 0f;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch uiBatch;
    private final BitmapFont font;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;

    private Player player;
    private final Array<Arrow> arrows = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private Boss boss;
    private final Array<Body> bodiesToDestroy = new Array<>();
    private final Array<Body> worldBodies = new Array<>();

    private static class ArrowSpawn {
        float x, y; boolean facingRight, isPlayer;
        ArrowSpawn(float x, float y, boolean fr, boolean ip) {
            this.x=x; this.y=y; this.facingRight=fr; this.isPlayer=ip;
        }
    }
    private final Array<ArrowSpawn> arrowsToSpawn = new Array<>();

    // Juice (partículas)
    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        final Color color;
        Particle(float x, float y, float vx, float vy, float life, Color c) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.color = c;
        }
    }
    private final Array<Particle> particles = new Array<>();
    private float shakeTime = 0f, shakeMag = 0f;
    private float hitFlash = 0f;
    private int prevHp = Constants.PLAYER_MAX_HP;
    private boolean prevOnGround = true;

    private final TouchControls touchControls;
    private final Hud hud;

    private float fireCooldown = 0f;
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;
    private float checkpointX = 2500f;
    private Color backgroundColor;

    // US-12 — Fase visual
    private final GameArt art;
    private final Background background;
    private final SpriteBatch worldBatch;
    private boolean debugDraw = false; // F1 alterna Fase 1 (debug) / Fase 2 (sprites)

    public GameScreen(KayabGame game, SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        this.shapeRenderer = new ShapeRenderer();
        this.uiBatch = new SpriteBatch();
        this.worldBatch = new SpriteBatch();
        this.art = new GameArt();
        this.background = new Background();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, uiCamera);
        this.touchControls = new TouchControls();
        this.hud = new Hud();
        initLevel();
    }

    private void initLevel() {
        if (world != null) world.dispose();
        world = new World(new Vector2(0, -9.8f), true);
        world.setContactListener(new ContactListenerManager());
        debugRenderer = new Box2DDebugRenderer();
        arrows.clear();
        enemies.clear();
        boss = null;
        bodiesToDestroy.clear();
        arrowsToSpawn.clear();
        levelComplete = false;
        levelCompleteTimer = 0f;

        player = new Player(world, 50, 60);
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        // Juice reset
        particles.clear();
        shakeTime = 0f; shakeMag = 0f; hitFlash = 0f;
        prevHp = player.getHp();
        prevOnGround = true;

        setupWorldTheme(saveData.currentWorld);
        loadLevelLayout(saveData.currentWorld);
    }

    private void setupWorldTheme(int worldNum) {
        switch (worldNum) {
            case 1: backgroundColor = new Color(0.28f, 0.56f, 0.25f, 1f); break;
            case 2: backgroundColor = new Color(0.28f, 0.45f, 0.35f, 1f); break;
            case 3: backgroundColor = new Color(0.55f, 0.25f, 0.05f, 1f); break;
            case 4: backgroundColor = new Color(0.25f, 0.25f, 0.30f, 1f); break;
            default: backgroundColor = Color.BLACK;
        }
    }

    private void loadLevelLayout(int worldNum) {
        switch (worldNum) {
            case 1:
                checkpointX = 1600f;
                // Suelo 1: 0 a 480
                EntityFactory.createPlatform(world, 240, 20, 480, 10);
                // HUECO 80px (480 a 560)
                // Suelo 2: 560 a 1040
                EntityFactory.createPlatform(world, 800, 20, 480, 10);
                // HUECO 60px (1040 a 1100)
                // Suelo 3: 1100 a 1800
                EntityFactory.createPlatform(world, 1450, 20, 700, 10);

                // Plataformas elevadas (Y=42, suelo es 25 -> Delta 17px, Kayab salta 26px)
                EntityFactory.createPlatform(world, 400, 42, 80, 6);
                spawnEnemy(400, 61, Enemy.Type.BASIC, 30); // Sobre plataforma (45 top + 16 center)

                EntityFactory.createPlatform(world, 800, 42, 80, 6);
                spawnEnemy(800, 61, Enemy.Type.SHIELD, 30);

                spawnEnemy(650, 41, Enemy.Type.BASIC, 50); // En suelo (25 top + 16 center)
                break;

            case 2:
                // Nivel extendido a x≈2800. Checkpoint en x=1800 (hay contenido después).
                checkpointX = 1800f;

                // =========================================================
                // SUELO — 3 tramos separados por huecos imposibles
                // =========================================================
                EntityFactory.createPlatform(world, 240,  20, 480, 10);  // x=0   → x=480
                // GAP MORTAL 90px (x=480 → x=570)
                EntityFactory.createPlatform(world, 710,  20, 280, 10);  // x=570 → x=850
                // GAP MORTAL 100px (x=850 → x=950)
                EntityFactory.createPlatform(world, 1075, 20, 250, 10);  // x=950 → x=1200
                // GAP SALTABLE 46px (x=1200 → x=1246) — crea tensión en la transición
                EntityFactory.createPlatform(world, 1371, 20, 280, 10);  // x=1246 → x=1526 (← lanzadera al hueco 3)
                // GAP MORTAL 84px (x=1526 → x=1610)
                EntityFactory.createPlatform(world, 1852, 20, 260, 10);  // x=1610 → x=1870
                // GAP SALTABLE 52px (x=1870 → x=1922) — penúltimo obstáculo
                EntityFactory.createPlatform(world, 2061, 20, 740, 10);  // x=1922 → x=2800 (recta final + boss room)

                // =========================================================
                // ZONA 1 — Escalada obligatoria sobre HUECO MORTAL 1 (90px)
                // Plataformas en zigzag ascendente. Enemigo BASIC en el pico.
                // =========================================================
                // A: lanzadera (Δ20px sobre suelo izq) — borde en x=472
                EntityFactory.createPlatform(world, 444, 40, 64, 6);     // x=412-476, Y=40
                // B: Δ18px más alto (Y=58), gap borde-a-borde = 56px → saltable
                EntityFactory.createPlatform(world, 532, 58, 64, 6);     // x=500-564, Y=58
                spawnEnemy(564, 73, Enemy.Type.BASIC, 22);               // empuja al borde derecho (hacia el vacío)
                // C: Δ14px más alto (Y=72), gap = 56px → saltable — pico de la secuencia
                EntityFactory.createPlatform(world, 620, 72, 68, 6);     // x=586-654 — sobre el vacío
                // Desde C, caída libre a suelo x=570+

                // =========================================================
                // ZONA 2 — Puente de plataformas sobre HUECO MORTAL 2 (100px)
                // Hay un SHIELD en la lanzadera que presiona al jugador a saltar rápido.
                // =========================================================
                // D: lanzadera en suelo derecho del gap1 (Y=42)
                EntityFactory.createPlatform(world, 790, 42, 58, 6);     // x=761-819
                spawnEnemy(790, 57, Enemy.Type.SHIELD, 20);              // escudo en la lanzadera, presiona
                // E: sobre el hueco 2, gap borde-a-borde = 52px → saltable (Y=56)
                EntityFactory.createPlatform(world, 871, 56, 60, 6);     // x=841-901
                // F: bajada (Y=42), gap = 50px → saltable; aterriza en suelo x=950+
                EntityFactory.createPlatform(world, 951, 42, 60, 6);     // x=921-981

                // =========================================================
                // ZONA 3 — Combo en suelo x=950→x=1200: grupo de enemigos
                // + plataforma con GUN que hostigua mientras esquivas en suelo
                // =========================================================
                spawnEnemy(1010, 35, Enemy.Type.BASIC,  60);// novedad: SWORD

                // Plataforma elevada con GUN apuntando a la zona de suelo
                EntityFactory.createPlatform(world, 1140, 42, 68, 6);    // x=1106-1174, Y=42
                spawnEnemy(1140, 57, Enemy.Type.GUN, 24);                // dispara hacia x<1106 — área de pelea

                // =========================================================
                // GAP SALTABLE x=1200→1246 (46px) — pequeña pausa de ritmo
                // Antes del gap hay una plataforma baja con item visual
                // =========================================================
                EntityFactory.createPlatform(world, 1178, 38, 52, 6);    // lanzadera al gap (Y=38, Δ18)

                // =========================================================
                // ZONA 4 — Suelo x=1246→x=1526: densidad media-alta
                // Tres plataformas en L invertida: subir, pelear, bajar al siguiente hueco
                // =========================================================
                spawnEnemy(1300, 35, Enemy.Type.BASIC, 80);

                // Plataforma isla con dos enemigos (BASIC + SHIELD espalda con espalda)
                EntityFactory.createPlatform(world, 1370, 44, 90, 6);    // x=1325-1415, Y=44
                spawnEnemy(1400, 59, Enemy.Type.SHIELD, 30);             // los dos se mueven en la misma plataforma

                // Plataforma más alta con GUN mirando hacia la derecha
                EntityFactory.createPlatform(world, 1460, 58, 68, 6);    // x=1426-1494, Y=58
                spawnEnemy(1460, 73, Enemy.Type.GUN, 22);                // dispara hacia la recta de suelo siguiente
                // guarda el borde del hueco 3

                // =========================================================
                // ZONA 5 — Cruce HUECO MORTAL 3 (84px, x=1526→x=1610)
                // Plataformas más apretadas — el jugador ya debería saber la mecánica
                // =========================================================
                // G: lanzadera (Y=42), borde en x=1522
                EntityFactory.createPlatform(world, 1494, 42, 54, 6);    // x=1467-1521
                // H: sobre el hueco (Y=56), gap=51px → saltable
                EntityFactory.createPlatform(world, 1572, 56, 56, 6);    // x=1544-1600
    // x=1787-1853, Y=44

                break;

            case 3:
                // =========================================================
                // MUNDO 3 — "El Corazón del Imperio"
                // Combate AÉREO sobre el vacío (caída = muerte instantánea,
                // Player muere si y < -32px) y arena de boss en el PISO.
                //
                // Física de referencia (Constants reales):
                //   PLAYER_SPEED 3.2 / JUMP_IMPULSE 4.2  → salto ~29px de alto,
                //   alcance horizontal ~85px (huecos ≤55px son cómodos).
                //   Plataforma: top = centerY + alto/2 ; Player descansa en top+16.
                //   Enemigo patrulla ±patrolRange → patrolRange ≤ (anchoMitad - 7)
                //   para NO caerse de la plataforma.
                // =========================================================
                checkpointX = 2000f; // bandera detrás del boss: hay que pasarlo

                // --- SUELO DE INICIO (sólido) — único respiro antes del vacío
                EntityFactory.createPlatform(world, 200, 20, 400, 10);   // x=0→400, top=25

                // =========================================================
                // EL VACÍO: desde x=400 NO hay suelo hasta la arena del boss.
                // Solo plataformas flotantes. Fallar un salto = caer y morir.
                // Todos los huecos (borde-a-borde) ≤50px → saltables; los
                // desniveles entre topes ≤12px → muy por debajo del salto de 29px.
                // =========================================================
                // P1 (lanzadera): borde suelo x=400 → hueco 30px, Δ+12
                EntityFactory.createPlatform(world, 470, 34, 80, 6);     // x=430→510, top=37
                spawnEnemy(470, 53, Enemy.Type.BASIC, 28);              // arquero azteca sobre el vacío

                // P2 (respiro / punto de puntería) — hueco 50px, Δ+8
                EntityFactory.createPlatform(world, 600, 42, 80, 6);     // x=560→640, top=45

                // P3 — hueco 50px, Δ+8
                EntityFactory.createPlatform(world, 730, 50, 80, 6);     // x=690→770, top=53
                spawnEnemy(730, 69, Enemy.Type.BASIC, 28);

                // P4 (bajada) — hueco 50px, Δ-6
                EntityFactory.createPlatform(world, 860, 44, 80, 6);     // x=820→900, top=47
                spawnEnemy(860, 63, Enemy.Type.BASIC, 28);

                // P5 — hueco 50px, Δ+8
                EntityFactory.createPlatform(world, 990, 52, 80, 6);     // x=950→1030, top=55
                spawnEnemy(990, 71, Enemy.Type.BASIC, 28);

                // P6 (respiro / puntería) — hueco 50px, Δ-6
                EntityFactory.createPlatform(world, 1120, 46, 80, 6);    // x=1080→1160, top=49

                // P7 (más ancha) — hueco 45px, Δ+8
                EntityFactory.createPlatform(world, 1250, 54, 90, 6);    // x=1205→1295, top=57
                spawnEnemy(1250, 73, Enemy.Type.BASIC, 30);

                // P8 (trampolín final hacia la arena) — hueco 35px, Δ-6
                EntityFactory.createPlatform(world, 1380, 48, 100, 6);   // x=1330→1430, top=51

                // =========================================================
                // ARENA DEL BOSS (suelo sólido) — se baja con un salto desde P8
                // (hueco 20px, caída de 26px hacia el piso, aterrizaje seguro).
                // Aquí la pelea es EN EL PISO, como pediste.
                // =========================================================
                EntityFactory.createPlatform(world, 1750, 20, 600, 10);  // x=1450→2050, top=25

                // Guardián Escudo en la entrada de la arena (avanza hacia ti;
                // a salvo de caerse porque el suelo es ancho).
                spawnEnemy(1550, 41, Enemy.Type.SHIELD, 35);

                // Xócotl en el piso de la arena (top 25 + 20 de medio cuerpo = 45)
                boss = new Boss(EntityFactory.createBossBody(world, 1900, 46, Boss.Type.XOCOTL), Boss.Type.XOCOTL);
                break;

            case 4:
                // =========================================================
                // MUNDO 4 — "La Traición"
                // Asalto TERRESTRE a las líneas españolas: suelo firme con
                // soldados de espada (SWORD), pozos mortales puntuales,
                // arcabuceros (GUN) hostigando, y una COLUMNA de cobertura
                // (las flechas mueren al tocar plataforma → tapa el disparo).
                // Cierre: Vargas en una arena amplia, pelea en el PISO.
                //
                // Física: salto ~29px alto / ~85px alcance; hueco ≤55px ok;
                // top = centerY + alto/2; descanso en top+16; caída (y<-32) = muerte.
                // =========================================================
                checkpointX = 2200f; // bandera detrás de Vargas

                // --- SEG 1: Suelo inicial — primer soldado de avanzada
                EntityFactory.createPlatform(world, 250, 20, 500, 10);   // x=0→500, top=25
                spawnEnemy(350, 41, Enemy.Type.SWORD, 60);              // soldado español en el piso

                // --- SEG 2: POZO MORTAL 1 (vacío x=500→770) con arcabucero hostigando
                EntityFactory.createPlatform(world, 595, 28, 90, 6);     // PA x=550→640, top=31 (hueco 50, Δ+6)
                spawnEnemy(595, 47, Enemy.Type.GUN, 30);                // arcabucero sobre el pozo
                EntityFactory.createPlatform(world, 720, 24, 90, 6);     // PB x=675→765, top=27 (hueco 35, Δ-4)

                // --- SEG 3: Suelo medio con COLUMNA de cobertura + arcabucero detrás
                EntityFactory.createPlatform(world, 960, 20, 380, 10);   // G2 x=770→1150, top=25 (continúa)
                spawnEnemy(880, 41, Enemy.Type.SWORD, 50);              // soldado en campo abierto
                // Columna baja (tapa la flecha enemiga que viaja a y~41; se puede
                // escalar de un salto: top=44 < 54px de alcance del salto).
                EntityFactory.createPlatform(world, 1020, 32, 12, 24);   // COLUMNA x=1014→1026, abarca y=20→44
                spawnEnemy(1080, 41, Enemy.Type.GUN, 25);               // arcabucero cubierto por la columna

                // --- SEG 4: POZO MORTAL 2 (vacío x=1150→1550) — saltos sobre el vacío
                EntityFactory.createPlatform(world, 1240, 30, 80, 6);    // PC x=1200→1280, top=33 (hueco 50, Δ+8)
                spawnEnemy(1240, 49, Enemy.Type.GUN, 28);               // arcabucero sobre el vacío
                EntityFactory.createPlatform(world, 1370, 36, 80, 6);    // PD x=1330→1410, top=39 (hueco 50, Δ+6) respiro
                EntityFactory.createPlatform(world, 1500, 30, 80, 6);    // PE x=1460→1540, top=33 (hueco 50, Δ-6)
                spawnEnemy(1500, 49, Enemy.Type.SWORD, 28);             // soldado guardando el último salto

                // --- SEG 5: ARENA DE VARGAS (suelo sólido y ancho — pelea en el piso)
                EntityFactory.createPlatform(world, 1900, 20, 700, 10);  // GA x=1550→2250, top=25 (caída 8px desde PE)
                spawnEnemy(1680, 41, Enemy.Type.SWORD, 50);             // guardia en la entrada de la arena
                // Vargas en el piso (top 25 + 19 de medio cuerpo = 44)
                boss = new Boss(EntityFactory.createBossBody(world, 2050, 44, Boss.Type.VARGAS), Boss.Type.VARGAS);
                break;
        }
    }

    private void spawnEnemy(float x, float y, Enemy.Type type, float patrolRange) {
        Body body = EntityFactory.createEnemyBody(world, x, y);
        enemies.add(new Enemy(body, type, patrolRange));
    }

    // ---------- Juice ----------
    private void shake(float mag, float time) {
        if (mag > shakeMag) shakeMag = mag;
        if (time > shakeTime) shakeTime = time;
    }

    private void burst(float xPx, float yPx, Color c, int n, float speed) {
        for (int i = 0; i < n; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float sp = MathUtils.random(speed * 0.4f, speed);
            particles.add(new Particle(xPx, yPx,
                    MathUtils.cos(ang) * sp, MathUtils.sin(ang) * sp + 40f,
                    MathUtils.random(0.3f, 0.6f), c));
        }
    }

    private void updateParticles(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= delta;
            if (p.life <= 0) { particles.removeIndex(i); continue; }
            p.vy -= 240f * delta; // gravedad
            p.x += p.vx * delta;
            p.y += p.vy * delta;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // US17: ESC vuelve al menú principal
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.showMenu(); return; }

        if (!player.isAlive()) {
            renderMessage("PLAYER DEFEATED", "TOUCH TO RETRY", Color.RED);
            if (Gdx.input.justTouched()) initLevel();
            return;
        }

        if (levelComplete) {
            levelCompleteTimer += delta;
            renderMessage("CONGRATULATIONS!", "PASSED LEVEL " + saveData.currentWorld, Color.YELLOW);
            if (levelCompleteTimer > 3f) {
                if (saveData.currentWorld < 4) {
                    saveData.currentWorld++;
                    game.getDatabase().updateSave(saveData);
                    initLevel();
                } else {
                    renderMessage("KAYAB: HERO OF THUNDER", "THE END", Color.GOLD);
                    if (levelCompleteTimer > 6f) Gdx.app.exit();
                }
            }
            return;
        }

        // US-12: alternar Fase 1 (debug shapes) / Fase 2 (sprites)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) debugDraw = !debugDraw;

        touchControls.update();
        boolean moveLeft  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || touchControls.moveLeft;
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || touchControls.moveRight;
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || touchControls.jumpRequest;
        boolean fire = Gdx.input.isKeyJustPressed(Input.Keys.K) || Gdx.input.isKeyJustPressed(Input.Keys.Z) || touchControls.fireHeld;

        if (fireCooldown > 0f) fireCooldown -= delta;
        if (fire && fireCooldown <= 0f) {
            shootArrow(player.getPosition().x * Constants.PPM, player.getPosition().y * Constants.PPM, player.isFacingRight(), true);
            player.onShoot();
            fireCooldown = Constants.ARROW_COOLDOWN;
        }

        accumulator += Math.min(delta, 0.25f);
        while (accumulator >= FIXED_STEP) {
            float camLeft = (camera.position.x - Constants.VIRTUAL_WIDTH / 2f) / Constants.PPM;
            player.update(FIXED_STEP, moveLeft, moveRight, jump, camLeft);
            for (Enemy e : enemies) {
                e.update(FIXED_STEP, player.getPosition());
                if (e.takeShootRequest()) encolarDisparo(e.getBody(), e.isFacingRight(), false);
            }
            if (boss != null) {
                boss.update(FIXED_STEP, player.getPosition());
                if (boss.takeShootRequest()) encolarDisparo(boss.getBody(), boss.isFacingRight(), false);
            }
            for (Arrow a : arrows) a.update();
            world.step(FIXED_STEP, 6, 2);
            accumulator -= FIXED_STEP;
        }

        for (ArrowSpawn s : arrowsToSpawn) shootArrow(s.x, s.y, s.facingRight, s.isPlayer);
        arrowsToSpawn.clear();
        cleanupEntities();
        checkCheckpoint();

        if (player.isAlive() && player.getPosition().x * Constants.PPM > camera.position.x) {
            camera.position.x = player.getPosition().x * Constants.PPM;
        }
        camera.update();

        // Juice: partículas + eventos (golpe, aterrizaje)
        updateParticles(delta);
        if (player.getHp() < prevHp) { shake(5f, 0.3f); hitFlash = 0.25f; }
        prevHp = player.getHp();
        boolean onG = player.isOnGround();
        if (onG && !prevOnGround) {
            burst(player.getPosition().x * Constants.PPM, player.getPosition().y * Constants.PPM - 16f,
                  new Color(0.70f, 0.60f, 0.45f, 1f), 6, 55f);
        }
        prevOnGround = onG;
        if (hitFlash > 0) hitFlash -= delta;

        // Aplicar shake a la cámara (se restaura tras dibujar el mundo)
        float shakeX = 0f, shakeY = 0f;
        if (shakeTime > 0) {
            shakeTime -= delta;
            shakeX = MathUtils.random(-shakeMag, shakeMag);
            shakeY = MathUtils.random(-shakeMag, shakeMag);
            if (shakeTime <= 0) shakeMag = 0f;
            camera.position.add(shakeX, shakeY, 0);
            camera.update();
        }

        world.getBodies(worldBodies);

        if (debugDraw) {
            // ===== FASE 1: figuras de depuración (ShapeRenderer + Box2DDebugRenderer) =====
            shapeRenderer.setProjectionMatrix(camera.combined);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.3f);
            for (int i = 0; i < 100; i++) shapeRenderer.rect(i * 100, 0, 2, 10);

            for (Body b : worldBodies) {
                if ("platform".equals(b.getUserData())) {
                    shapeRenderer.setColor(Color.LIGHT_GRAY);
                    for (Fixture f : b.getFixtureList()) {
                        if (f.getShape() instanceof PolygonShape) {
                            PolygonShape s = (PolygonShape) f.getShape();
                            Vector2 v = new Vector2(); s.getVertex(0, v);
                            float hW = Math.abs(v.x), hH = Math.abs(v.y);
                            shapeRenderer.rect((b.getPosition().x - hW) * Constants.PPM, (b.getPosition().y - hH) * Constants.PPM, hW * 2 * Constants.PPM, hH * 2 * Constants.PPM);
                        }
                    }
                }
            }

            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(checkpointX, 25, 5, 40);
            shapeRenderer.rect(checkpointX + 5, 45, 15, 10);

            player.renderDebug(shapeRenderer);
            for (Enemy enemy : enemies) enemy.render(shapeRenderer);
            if (boss != null) boss.render(shapeRenderer);
            for (Arrow arrow : arrows) arrow.render(shapeRenderer);

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            debugRenderer.render(world, camera.combined);
        } else {
            // ===== FASE 2: sprites (SpriteBatch + TextureRegion) =====
            // Fondo parallax (proyección de pantalla, su propio begin/end)
            background.render(worldBatch, saveData.currentWorld, camera.position.x);

            worldBatch.setProjectionMatrix(camera.combined);
            worldBatch.begin();

            // Plataformas — tile de terreno repetido en horizontal (no estirado)
            TextureRegion tile = art.getTile(saveData.currentWorld);
            for (Body b : worldBodies) {
                if ("platform".equals(b.getUserData())) {
                    for (Fixture f : b.getFixtureList()) {
                        if (f.getShape() instanceof PolygonShape) {
                            PolygonShape s = (PolygonShape) f.getShape();
                            Vector2 v = new Vector2(); s.getVertex(0, v);
                            float hW = Math.abs(v.x), hH = Math.abs(v.y);
                            float left = (b.getPosition().x - hW) * Constants.PPM;
                            float bottom = (b.getPosition().y - hH) * Constants.PPM;
                            float pw = hW * 2 * Constants.PPM;
                            float ph = hH * 2 * Constants.PPM;
                            float tw = 16f;
                            for (float drawn = 0f; drawn < pw; drawn += tw) {
                                float seg = Math.min(tw, pw - drawn);
                                if (seg >= tw) {
                                    worldBatch.draw(tile, left + drawn, bottom, tw, ph);
                                } else {
                                    int pwPx = Math.max(1, Math.round(tile.getRegionWidth() * (seg / tw)));
                                    TextureRegion part = new TextureRegion(tile, 0, 0, pwPx, tile.getRegionHeight());
                                    worldBatch.draw(part, left + drawn, bottom, seg, ph);
                                }
                            }
                        }
                    }
                }
            }

            // Bandera de checkpoint
            worldBatch.setColor(Color.WHITE);
            worldBatch.draw(art.getWhite(), checkpointX, 25, 5, 40);
            worldBatch.setColor(0.82f, 0.23f, 0.23f, 1f);
            worldBatch.draw(art.getWhite(), checkpointX + 5, 45, 15, 10);
            worldBatch.setColor(Color.WHITE);

            // Entidades (Kayab encima de todo)
            for (Enemy enemy : enemies) enemy.renderSprite(worldBatch, art);
            if (boss != null) boss.renderSprite(worldBatch, art);
            for (Arrow arrow : arrows) arrow.renderSprite(worldBatch, art);
            player.renderSprite(worldBatch, art);

            // Partículas
            for (Particle p : particles) {
                float a = Math.max(0f, p.life / p.maxLife);
                float sz = 1.5f + 2.5f * a;
                worldBatch.setColor(p.color.r, p.color.g, p.color.b, a);
                worldBatch.draw(art.getWhite(), p.x - sz / 2f, p.y - sz / 2f, sz, sz);
            }
            worldBatch.setColor(Color.WHITE);

            worldBatch.end();
        }

        // Restaurar la cámara tras el shake (no afectar la lógica del próximo frame)
        if (shakeX != 0f || shakeY != 0f) {
            camera.position.sub(shakeX, shakeY, 0);
            camera.update();
        }

        // Destello rojo al recibir daño (espacio de pantalla)
        if (hitFlash > 0f) {
            uiViewport.apply();
            worldBatch.setProjectionMatrix(uiCamera.combined);
            worldBatch.begin();
            worldBatch.setColor(1f, 0.1f, 0.1f, Math.min(0.5f, hitFlash));
            worldBatch.draw(art.getWhite(), 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
            worldBatch.setColor(Color.WHITE);
            worldBatch.end();
        }

        touchControls.render(shapeRenderer);
        hud.render(shapeRenderer, player.getHp(), saveData.score, saveData.playerName);
    }

    private void renderMessage(String title, String subtitle, Color color) {
        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();
        font.setColor(color);
        font.draw(uiBatch, title, Constants.VIRTUAL_WIDTH / 2f - 80, Constants.VIRTUAL_HEIGHT / 2f + 20);
        font.setColor(Color.WHITE);
        font.draw(uiBatch, subtitle, Constants.VIRTUAL_WIDTH / 2f - 70, Constants.VIRTUAL_HEIGHT / 2f - 10);
        uiBatch.end();
    }

    private void encolarDisparo(Body b, boolean fr, boolean ip) {
        arrowsToSpawn.add(new ArrowSpawn(b.getPosition().x * Constants.PPM, b.getPosition().y * Constants.PPM, fr, ip));
    }

    private void shootArrow(float x, float y, boolean facingRight, boolean isPlayer) {
        float spawnX = x + (facingRight ? 12 : -12);
        float range = isPlayer ? Constants.ARROW_MAX_RANGE : Constants.ENEMY_ARROW_RANGE;
        Body arrowBody = EntityFactory.createArrowBody(world, spawnX, y, facingRight);
        arrows.add(new Arrow(arrowBody, range, isPlayer));
    }

    private void checkCheckpoint() {
        if (!levelComplete && player.getPosition().x * Constants.PPM >= checkpointX) {
            levelComplete = true;
            saveData.score += 500;
            Gdx.app.log("Kayab", "¡Nivel " + saveData.currentWorld + " completado!");
        }
    }

    private void cleanupEntities() {
        for (int i = arrows.size - 1; i >= 0; i--) { if (!arrows.get(i).isActive()) { bodiesToDestroy.add(arrows.get(i).getBody()); arrows.removeIndex(i); } }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (!e.isActive()) {
                Vector2 ep = e.getBody().getPosition();
                burst(ep.x * Constants.PPM, ep.y * Constants.PPM, new Color(1f, 0.5f, 0.2f, 1f), 9, 95f);
                shake(3f, 0.18f);
                bodiesToDestroy.add(e.getBody());
                enemies.removeIndex(i);
            }
        }
        if (boss != null && !boss.isActive()) {
            Vector2 bp = boss.getBody().getPosition();
            burst(bp.x * Constants.PPM, bp.y * Constants.PPM, new Color(1f, 0.85f, 0.3f, 1f), 26, 140f);
            shake(7f, 0.5f);
            bodiesToDestroy.add(boss.getBody());
            boss = null;
            // Recompensa por derrotar al boss (Xócotl=Mundo 3, Vargas=Mundo 4)
            saveData.score += (saveData.currentWorld == 4)
                ? Constants.SCORE_BOSS_VARGAS : Constants.SCORE_BOSS_XOCOTL;
        }
        for (Body b : bodiesToDestroy) world.destroyBody(b);
        bodiesToDestroy.clear();
    }

    @Override public void resize(int w, int h) {
        viewport.update(w, h);
        uiViewport.update(w, h, true);
        touchControls.resize(w, h);
        hud.resize(w, h);
    }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (world != null) world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
        hud.dispose();
        uiBatch.dispose();
        worldBatch.dispose();
        art.dispose();
        background.dispose();
        font.dispose();
    }
}
