package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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

/**
 * US17 — Pantalla de nombre (estilo minimalista, sin cajas).
 * Solo el fondo, el nombre con cursor parpadeante y opciones en texto plano.
 * Escribe con teclado físico (PC) o toca el nombre para el teclado de Android.
 */
public class NewGameScreen implements Screen {

    private static final int MAX_LEN = 14;

    private final KayabGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final Background bg = new Background();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 tmp = new Vector2();

    private final StringBuilder name = new StringBuilder();
    private float bgScroll = 0f, blink = 0f;
    private boolean confirmRequested = false, backRequested = false;

    // Rects calculados al dibujar (para el toque)
    private float nameX, nameW, nameTopY, okX, okW, okTopY, backX, backW, backTopY;

    public NewGameScreen(KayabGame game) {
        this.game = game;
    }

    private final InputAdapter typing = new InputAdapter() {
        @Override public boolean keyTyped(char c) {
            if (c == '\b') { if (name.length() > 0) name.deleteCharAt(name.length() - 1); }
            else if (c == '\r' || c == '\n') { confirmRequested = true; }
            else if (c >= 32 && c != 127 && name.length() < MAX_LEN) { name.append(c); }
            return true;
        }
        @Override public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) backRequested = true;
            return true;
        }
    };

    private void openSoftKeyboard() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override public void input(String text) {
                if (text == null) return;
                name.setLength(0);
                name.append(text.length() > MAX_LEN ? text.substring(0, MAX_LEN) : text);
            }
            @Override public void canceled() {}
        }, "Ingresa tu nombre, guerrero", name.toString(), "Kayab");
    }

    @Override
    public void render(float delta) {
        bgScroll += delta * 10f;
        blink += delta;

        Gdx.gl.glClearColor(0.05f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        bg.render(batch, 1, bgScroll);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.getData().setScale(1f);
        shadowCentered("¿cómo te llamas, guerrero?", Constants.VIRTUAL_HEIGHT - 30, new Color(0.96f, 0.93f, 0.86f, 1f));

        // Nombre con cursor parpadeante (placeholder gris si está vacío)
        font.getData().setScale(1.8f);
        boolean empty = name.length() == 0;
        String shown = empty ? "kayab" : name.toString();
        if (!empty && (int) (blink * 2) % 2 == 0) shown = shown + "_";
        layout.setText(font, shown);
        nameW = layout.width; nameX = (Constants.VIRTUAL_WIDTH - nameW) / 2f; nameTopY = 104f;
        shadowText(shown, nameX, nameTopY, empty ? new Color(1f, 1f, 1f, 0.4f) : Color.WHITE);

        // Acciones en texto plano
        font.getData().setScale(1f);
        layout.setText(font, "confirmar");
        okW = layout.width; okX = (Constants.VIRTUAL_WIDTH - okW) / 2f; okTopY = 52f;
        shadowText("confirmar", okX, okTopY, Color.WHITE);

        layout.setText(font, "volver");
        backW = layout.width; backX = (Constants.VIRTUAL_WIDTH - backW) / 2f; backTopY = 32f;
        shadowText("volver", backX, backTopY, new Color(1f, 1f, 1f, 0.55f));

        font.getData().setScale(0.7f);
        shadowCentered("escribe en pc · toca el nombre para el teclado del cel", 14, new Color(0.8f, 0.8f, 0.8f, 1f));
        font.getData().setScale(1f);
        batch.end();

        handleTouch();

        if (confirmRequested) {
            confirmRequested = false;
            Gdx.input.setInputProcessor(null);
            String n = name.toString().trim();
            game.startNewGame(n.isEmpty() ? "Kayab" : n);
            return;
        }
        if (backRequested) {
            backRequested = false;
            Gdx.input.setInputProcessor(null);
            game.showMenu();
            return;
        }
    }

    private void handleTouch() {
        if (!Gdx.input.justTouched()) return;
        viewport.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
        if (inBand(tmp.x, tmp.y, nameX, nameW, nameTopY, 24)) openSoftKeyboard();
        else if (inBand(tmp.x, tmp.y, okX, okW, okTopY, 14)) confirmRequested = true;
        else if (inBand(tmp.x, tmp.y, backX, backW, backTopY, 14)) backRequested = true;
    }

    private boolean inBand(float px, float py, float x, float w, float topY, float h) {
        return px >= x - 6 && px <= x + w + 6 && py >= topY - h && py <= topY + 4;
    }

    private void shadowCentered(String text, float topY, Color color) {
        layout.setText(font, text);
        shadowText(text, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, topY, color);
    }

    private void shadowText(String text, float x, float topY, Color color) {
        font.setColor(0f, 0f, 0f, color.a * 0.8f);
        font.draw(batch, text, x + 1f, topY - 1f);
        font.setColor(color);
        font.draw(batch, text, x, topY);
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(typing); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bg.dispose();
    }
}
