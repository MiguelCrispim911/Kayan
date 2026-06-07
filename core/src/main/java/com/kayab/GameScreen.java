package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.entities.Player;
import com.kayab.ui.TouchControls;

public class GameScreen implements Screen {
    private KayabGame game;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Player player;
    private TouchControls touchControls;

    public GameScreen(KayabGame game) {
        this.game = game;
        this.world = new World(new Vector2(0, -9.8f), true);
        this.world.setContactListener(new ContactListenerManager());

        this.debugRenderer = new Box2DDebugRenderer();
        this.shapeRenderer = new ShapeRenderer();

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);

        this.touchControls = new TouchControls(viewport);

        // Spawn de Kayab
        this.player = new Player(world, 50, 100);

        // Plataforma inicial (etiquetada como "platform" para el ContactListener)
        createStaticPlatform(Constants.VIRTUAL_WIDTH / 2f, 20, Constants.VIRTUAL_WIDTH * 4, 10);
    }

    private void createStaticPlatform(float x, float y, float width, float height) {
        com.badlogic.gdx.physics.box2d.BodyDef bodyDef = new com.badlogic.gdx.physics.box2d.BodyDef();
        bodyDef.type = com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        com.badlogic.gdx.physics.box2d.Body body = world.createBody(bodyDef);
        body.setUserData("platform");

        com.badlogic.gdx.physics.box2d.PolygonShape shape = new com.badlogic.gdx.physics.box2d.PolygonShape();
        shape.setAsBox(width / 2f / Constants.PPM, height / 2f / Constants.PPM);
        body.createFixture(shape, 0.0f);
        shape.dispose();
    }

    @Override
    public void render(float delta) {
        // Color Fondo Mundo 1 (TDD): Verde selva
        Gdx.gl.glClearColor(0.28f, 0.56f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Input (Teclado + Touch)
        touchControls.update();
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || touchControls.moveLeft;
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || touchControls.moveRight;
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || touchControls.jumpRequest;

        // 2. Actualización de Física
        world.step(1/60f, 6, 2);

        float cameraLeftEdge = (camera.position.x - Constants.VIRTUAL_WIDTH / 2f) / Constants.PPM;
        player.update(moveLeft, moveRight, jump, cameraLeftEdge);

        // 3. Seguimiento de Cámara (Scroll unidireccional)
        if (player.getPosition().x * Constants.PPM > camera.position.x) {
            camera.position.x = player.getPosition().x * Constants.PPM;
        }

        // 4. Renderizado
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        // Dibujamos el suelo visualmente
        shapeRenderer.rect(-Constants.VIRTUAL_WIDTH, 15, Constants.VIRTUAL_WIDTH * 10, 10);
        player.renderDebug(shapeRenderer);
        shapeRenderer.end();

        touchControls.render(shapeRenderer);
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
