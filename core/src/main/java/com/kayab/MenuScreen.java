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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.gfx.Background;
import com.kayab.SaveSelectScreen;

/**
 * US17 — Menú principal (estilo minimalista).
 * Solo la floresta parallax del Mundo 1 y texto blanco simple, sin cajas ni
 * paneles. La opción elegida se marca con un › y un leve brillo.
 * Navegación por teclado (↑↓ + Enter) o tocando el texto.
 */
public class MenuScreen implements Screen {

    private final KayabGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final Background bg = new Background();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 tmp = new Vector2();

    private final String[] options;
    private final float[] optTopY, optX, optW; // calculados al dibujar, usados para el toque
    private int selected = 0;
    private float bgScroll = 0f;

    private static final float OPTIONS_TOP = 98f, GAP = 22f;

    public MenuScreen(KayabGame game) {
        this.game = game;
        boolean hasSave = game.getDatabase().loadLatestSave() != null;
        options = hasSave
                ? new String[]{"nueva partida", "continuar", "salir"}
                : new String[]{"nueva partida", "salir"};
        optTopY = new float[options.length];
        optX = new float[options.length];
        optW = new float[options.length];
    }

    @Override
    public void render(float delta) {
        bgScroll += delta * 10f;

        Gdx.gl.glClearColor(0.05f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        bg.render(batch, 1, bgScroll);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Título
        font.getData().setScale(2f);
        shadowCentered("KAYAB", Constants.VIRTUAL_HEIGHT - 22, new Color(0.96f, 0.93f, 0.86f, 1f));
        font.getData().setScale(0.8f);
        shadowCentered("hijo del trueno", Constants.VIRTUAL_HEIGHT - 44, new Color(0.85f, 0.85f, 0.85f, 1f));

        // Opciones
        font.getData().setScale(1f);
        for (int i = 0; i < options.length; i++) {
            float topY = OPTIONS_TOP - i * GAP;
            layout.setText(font, options[i]);
            float x = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
            optTopY[i] = topY; optX[i] = x; optW[i] = layout.width;

            boolean sel = (i == selected);
            shadowText(options[i], x, topY, sel ? Color.WHITE : new Color(1f, 1f, 1f, 0.55f));
            if (sel) shadowText("›", x - 12, topY, Color.WHITE); // marcador ›
        }
        font.getData().setScale(1f);
        batch.end();

        handleInput();
    }

    private void shadowCentered(String text, float topY, Color color) {
        layout.setText(font, text);
        shadowText(text, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, topY, color);
    }

    /** Texto con sombra de 1px para legibilidad sobre el fondo, sin cajas. */
    private void shadowText(String text, float x, float topY, Color color) {
        font.setColor(0f, 0f, 0f, color.a * 0.8f);
        font.draw(batch, text, x + 1f, topY - 1f);
        font.setColor(color);
        font.draw(batch, text, x, topY);
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
                // banda del texto (font.draw usa topY como borde superior)
                if (tmp.x >= optX[i] - 6 && tmp.x <= optX[i] + optW[i] + 6
                        && tmp.y >= optTopY[i] - 14 && tmp.y <= optTopY[i] + 4) {
                    selected = i;
                    activate(i);
                    return;
                }
            }
        }
    }

    private void activate(int index) {
        String opt = options[index];
        if ("nueva partida".equals(opt))  game.changeScreen(new NewGameScreen(game));
        else if ("continuar".equals(opt)) game.changeScreen(new com.kayab.SaveSelectScreen(game));
        else if ("salir".equals(opt))     Gdx.app.exit();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(null); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bg.dispose();
    }
}
