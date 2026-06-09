package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.gfx.Background;

/**
 * US17 — Menú principal.
 * Nueva Partida · Continuar (si hay save) · Salir.
 * Fondo: la floresta parallax del Mundo 1 (reutiliza Background) con un panel
 * oscuro para legibilidad. Navegación por teclado (↑↓ + Enter) o toque/click.
 */
public class MenuScreen implements Screen {

    private final KayabGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final Background bg = new Background();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 tmp = new Vector2();

    private final String[] options;
    private int selected = 0;
    private float bgScroll = 0f;
    private float blink = 0f;

    // Geometría de botones (coords virtuales, origen abajo-izq)
    private static final float BW = 168f, BH = 24f, BX = (Constants.VIRTUAL_WIDTH - BW) / 2f;
    private static final float START_Y = 92f, GAP = 30f;

    public MenuScreen(KayabGame game) {
        this.game = game;
        boolean hasSave = game.getDatabase().loadLatestSave() != null;
        options = hasSave
                ? new String[]{"NUEVA PARTIDA", "CONTINUAR", "SALIR"}
                : new String[]{"NUEVA PARTIDA", "SALIR"};
    }

    @Override
    public void render(float delta) {
        bgScroll += delta * 12f;
        blink += delta;

        Gdx.gl.glClearColor(0.05f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        // Fondo parallax (Background hace su propio begin/end)
        bg.render(batch, 1, bgScroll);

        // Panel oscuro para legibilidad
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        // Fondo de cada botón
        for (int i = 0; i < options.length; i++) {
            float by = START_Y - i * GAP;
            if (i == selected) shapes.setColor(0.85f, 0.65f, 0.20f, 0.92f);
            else               shapes.setColor(0.15f, 0.17f, 0.20f, 0.85f);
            shapes.rect(BX, by, BW, BH);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Textos
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(2f);
        drawCentered("KAYAB", Constants.VIRTUAL_HEIGHT - 18, new Color(0.95f, 0.85f, 0.45f, 1f));
        font.getData().setScale(1f);
        drawCentered("Hijo del Trueno", Constants.VIRTUAL_HEIGHT - 42, Color.WHITE);
        for (int i = 0; i < options.length; i++) {
            float by = START_Y - i * GAP;
            Color col = (i == selected) ? new Color(0.1f, 0.08f, 0.04f, 1f) : Color.WHITE;
            drawCentered(options[i], by + BH / 2f + 4f, col);
        }
        font.getData().setScale(0.8f);
        drawCentered("Flechas + Enter  /  toca para elegir", 16, new Color(0.8f, 0.8f, 0.8f, 1f));
        font.getData().setScale(1f);
        batch.end();

        handleInput();
    }

    private void drawCentered(String text, float topY, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        font.draw(batch, layout, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, topY);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
            selected = (selected - 1 + options.length) % options.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
            selected = (selected + 1) % options.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            activate(selected);
            return;
        }
        if (Gdx.input.justTouched()) {
            viewport.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
            for (int i = 0; i < options.length; i++) {
                float by = START_Y - i * GAP;
                if (tmp.x >= BX && tmp.x <= BX + BW && tmp.y >= by && tmp.y <= by + BH) {
                    selected = i;
                    activate(i);
                    return;
                }
            }
        }
    }

    private void activate(int index) {
        String opt = options[index];
        if ("NUEVA PARTIDA".equals(opt))      game.changeScreen(new NewGameScreen(game));
        else if ("CONTINUAR".equals(opt))     game.continueGame();
        else if ("SALIR".equals(opt))         Gdx.app.exit();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(null); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        bg.dispose();
    }
}
