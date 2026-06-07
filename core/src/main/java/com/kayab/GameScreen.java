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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.entities.Arrow;
import com.kayab.entities.Enemy;
import com.kayab.entities.EntityFactory;
import com.kayab.entities.Player;
import com.kayab.ui.TouchControls;

/**
 * Pantalla principal del juego — Fase 1 (ShapeRenderer + Box2DDebugRenderer).
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
    private final Array<Arrow> arrows;
    private final Array<Enemy> enemies;
    private final Array<Body> bodiesToDestroy;

    // Fix 1 Claude: Cola de disparos para evitar crear bodies durante world.step
    private static class ArrowSpawn {
        float x, y; boolean facingRight, isPlayer;
        ArrowSpawn(float x, float y, boolean fr, boolean ip) {
            this.x=x; this.y=y; this.facingRight=fr; this.isPlayer=ip;
        }
    }
    private final Array<ArrowSpawn> arrowsToSpawn;

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

        // Cámara del mundo
        camera   = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        // Controles
        touchControls = new TouchControls();

        // Entidades
        player = new Player(world, 50, 100);
        arrows = new Array<>();
        enemies = new Array<>();
        bodiesToDestroy = new Array<>();
        arrowsToSpawn = new Array<>();

        // US05: Suelo y plataformas
        EntityFactory.createPlatform(world, 5000, 20, 10000, 10);
        EntityFactory.createPlatform(world, 200, 70, 60, 10);
        EntityFactory.createPlatform(world, 300, 110, 60, 10);
        EntityFactory.createPlatform(world, 400, 150, 60, 10);

        // US07: Spawn de enemigos
        spawnEnemy(400, 40, Enemy.Type.BASIC, 100);
        spawnEnemy(800, 40, Enemy.Type.BASIC, 150);
        spawnEnemy(1200, 40, Enemy.Type.SHIELD, 80);
    }

    private void spawnEnemy(float x, float y, Enemy.Type type, float patrolRange) {
        Body body = EntityFactory.createEnemyBody(world, x, y);
        enemies.add(new Enemy(body, type, patrolRange));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.28f, 0.56f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touchControls.update();

        boolean moveLeft  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || touchControls.moveLeft;
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || touchControls.moveRight;
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || touchControls.jumpRequest;
        boolean fire = Gdx.input.isKeyJustPressed(Input.Keys.K) || Gdx.input.isKeyJustPressed(Input.Keys.Z) || touchControls.fireHeld;

        // US06: Lógica de disparo del jugador
        if (fireCooldown > 0f) fireCooldown -= delta;
        if (fire && fireCooldown <= 0f && player.isAlive()) {
            shootArrow(player.getPosition().x * Constants.PPM, player.getPosition().y * Constants.PPM, player.isFacingRight(), true);
            fireCooldown = Constants.ARROW_COOLDOWN;
        }

        // Física
        accumulator += Math.min(delta, 0.25f);
        while (accumulator >= FIXED_STEP) {
            float cameraLeftEdge = (camera.position.x - Constants.VIRTUAL_WIDTH / 2f) / Constants.PPM;

            // Fix: Añadido FIXED_STEP como primer argumento (delta)
            player.update(FIXED_STEP, moveLeft, moveRight, jump, cameraLeftEdge);

            // Actualizar enemigos
            for (Enemy enemy : enemies) {
                enemy.update(FIXED_STEP, player.getPosition());
                if (enemy.takeShootRequest()) {
                    arrowsToSpawn.add(new ArrowSpawn(
                        enemy.getBody().getPosition().x * Constants.PPM,
                        enemy.getBody().getPosition().y * Constants.PPM,
                        enemy.isFacingRight(), false
                    ));
                }
            }

            // Actualizar flechas
            for (Arrow arrow : arrows) {
                arrow.update();
            }

            world.step(FIXED_STEP, VELOCITY_IT, POSITION_IT);
            accumulator -= FIXED_STEP;
        }

        for (ArrowSpawn s : arrowsToSpawn) {
            shootArrow(s.x, s.y, s.facingRight, s.isPlayer);
        }
        arrowsToSpawn.clear();

        cleanupEntities();

        if (player.isAlive() && player.getPosition().x * Constants.PPM > camera.position.x) {
            camera.position.x = player.getPosition().x * Constants.PPM;
        }
        camera.update();

        // Render
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Visual Suelo y plataformas
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(-Constants.VIRTUAL_WIDTH, 15, Constants.VIRTUAL_WIDTH * 40, 10);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(170, 65, 60, 10);
        shapeRenderer.rect(270, 105, 60, 10);
        shapeRenderer.rect(370, 145, 60, 10);

        player.renderDebug(shapeRenderer);

        for (Enemy enemy : enemies) {
            enemy.render(shapeRenderer);
        }

        for (Arrow arrow : arrows) {
            arrow.render(shapeRenderer);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        debugRenderer.render(world, camera.combined);
        touchControls.render(shapeRenderer);
    }

    private void shootArrow(float x, float y, boolean facingRight, boolean isPlayer) {
        float spawnX = x + (facingRight ? 12 : -12);
        float spawnY = y;
        float range = isPlayer ? Constants.ARROW_MAX_RANGE : Constants.ENEMY_ARROW_RANGE;

        Body arrowBody = EntityFactory.createArrowBody(world, spawnX, spawnY, facingRight);
        arrows.add(new Arrow(arrowBody, range, isPlayer));
    }

    private void cleanupEntities() {
        for (int i = arrows.size - 1; i >= 0; i--) {
            Arrow arrow = arrows.get(i);
            if (!arrow.isActive()) {
                bodiesToDestroy.add(arrow.getBody());
                arrows.removeIndex(i);
            }
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (!enemy.isActive()) {
                bodiesToDestroy.add(enemy.getBody());
                enemies.removeIndex(i);
            }
        }

        for (Body body : bodiesToDestroy) {
            world.destroyBody(body);
        }
        bodiesToDestroy.clear();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        touchControls.resize(width, height);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }
}
