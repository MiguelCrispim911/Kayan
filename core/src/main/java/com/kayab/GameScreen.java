package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.entities.Player;
import com.kayab.ui.TouchControls;

/**
 * Pantalla principal del juego — Fase 1 (ShapeRenderer + Box2DDebugRenderer).
 *
 * CORRECCIONES respecto a la versión anterior:
 *  1. Accumulator pattern para world.step() — la física corre a velocidad correcta
 *     sin importar si el dispositivo va a 30, 60 o 120 fps.
 *  2. TouchControls tiene su propio viewport de UI fijo; se llama resize() en él.
 *  3. Disparo conectado al botón de TouchControls (fireHeld) y a teclado (K / Z).
 *  4. Blending GL habilitado antes de renderizar controles semitransparentes.
 *  5. Cámara nunca retrocede (posición X mínima bloqueada).
 */
public class GameScreen implements Screen {

    private final KayabGame game;

    // ── Física ──
    private final World world;
    private final Box2DDebugRenderer debugRenderer;

    // Accumulator pattern para timestep fijo
    private static final float FIXED_STEP   = 1f / 60f;
    private static final int   VELOCITY_IT  = 6;
    private static final int   POSITION_IT  = 2;
    private float accumulator = 0f;

    // ── Render ──
    private final ShapeRenderer shapeRenderer;

    // ── Cámara del mundo ──
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // ── Entidades ──
    private final Player player;

    // ── Controles ──
    private final TouchControls touchControls;

    // ── Cooldown de disparo ──
    private float fireCooldown = 0f;

    public GameScreen(KayabGame game) {
        this.game = game;

        // Mundo Box2D (gravedad estándar)
        world = new World(new Vector2(0, -9.8f), true);
        world.setContactListener(new ContactListenerManager());

        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

        // Cámara del mundo — coordenadas en píxeles virtuales
        camera   = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        // Controles (tienen su propio viewport UI interno)
        touchControls = new TouchControls();

        // Jugador — spawn en x=50, y=100 (px)
        player = new Player(world, 50, 100);

        // Plataforma de suelo inicial
        // Ancho = 100 tiles × 16px = 1600px (longitud mínima de nivel según TDD)
        createStaticPlatform(800, 20, 1600, 10);

        // Plataforma flotante de ejemplo
        createStaticPlatform(250, 80, 80, 10);
        createStaticPlatform(420, 110, 80, 10);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void createStaticPlatform(float cx, float cy, float w, float h) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(cx / Constants.PPM, cy / Constants.PPM);
        Body body = world.createBody(bd);
        body.setUserData("platform");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2f / Constants.PPM, h / 2f / Constants.PPM);
        body.createFixture(shape, 0f);
        shape.dispose();
    }

    // ── Ciclo principal ───────────────────────────────────────────────────────

    @Override
    public void render(float delta) {

        // 1. Limpiar pantalla — color de fondo Mundo 1 (verde selva, TDD)
        Gdx.gl.glClearColor(0.28f, 0.56f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Leer input
        touchControls.update();

        boolean moveLeft  = Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.A)
            || touchControls.moveLeft;

        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyPressed(Input.Keys.D)
            || touchControls.moveRight;

        // Salto: teclado usa isKeyJustPressed (1 frame), joystick ya es pulso en TouchControls
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || touchControls.jumpRequest;

        // Disparo: teclado K o Z, o botón táctil
        boolean fire = Gdx.input.isKeyJustPressed(Input.Keys.K)
            || Gdx.input.isKeyJustPressed(Input.Keys.Z)
            || touchControls.fireHeld;

        // 3. Cooldown de disparo
        if (fireCooldown > 0f) fireCooldown -= delta;
        if (fire && fireCooldown <= 0f) {
            // TODO US06: player.shoot() — por ahora solo log
            Gdx.app.log("Kayab", "¡Flecha disparada!");
            fireCooldown = Constants.ARROW_COOLDOWN;
        }

        // 4. Física — accumulator pattern (timestep fijo independiente de fps)
        accumulator += delta;
        while (accumulator >= FIXED_STEP) {
            // Borde izquierdo de cámara en metros (para clamp del jugador)
            float cameraLeftEdge = (camera.position.x - Constants.VIRTUAL_WIDTH / 2f) / Constants.PPM;
            player.update(moveLeft, moveRight, jump, cameraLeftEdge);
            world.step(FIXED_STEP, VELOCITY_IT, POSITION_IT);
            accumulator -= FIXED_STEP;
        }

        // 5. Cámara — scroll unidireccional (solo avanza a la derecha)
        float playerPx = player.getPosition().x * Constants.PPM;
        if (playerPx > camera.position.x) {
            camera.position.x = playerPx;
        }
        camera.update();

        // 6. Render del mundo (ShapeRenderer en coordenadas del mundo)
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Habilitar blending para cualquier color con alpha
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Suelo visual (gris oscuro)
        shapeRenderer.setColor(new Color(0.35f, 0.30f, 0.20f, 1f));
        shapeRenderer.rect(-Constants.VIRTUAL_WIDTH, 15,
            Constants.VIRTUAL_WIDTH * 10, 10);

        // Plataformas de ejemplo (color tierra)
        shapeRenderer.setColor(new Color(0.45f, 0.35f, 0.20f, 1f));
        shapeRenderer.rect(210 / Constants.PPM * Constants.PPM, 75,  80, 10);
        shapeRenderer.rect(380 / Constants.PPM * Constants.PPM, 105, 80, 10);

        // Jugador (rectángulo azul + línea de dirección)
        player.renderDebug(shapeRenderer);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 7. Box2D debug (hitboxes)
        debugRenderer.render(world, camera.combined);

        // 8. Controles táctiles — render sobre todo, con su viewport propio
        touchControls.render(shapeRenderer);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        touchControls.resize(width, height); // ← viewport UI también se actualiza
    }

    @Override public void show()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void hide()    {}

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }
}
