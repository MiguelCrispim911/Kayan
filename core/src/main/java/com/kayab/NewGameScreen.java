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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.gfx.Background;

/**
 * US17 — Pantalla de nombre.
 * Escribe con teclado físico (Desktop) o toca el campo para abrir el teclado
 * virtual de Android (Gdx.input.getTextInput). Confirmar crea la partida.
 */
public class NewGameScreen implements Screen {

    private static final int MAX_LEN = 14;

    private final KayabGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final Background bg = new Background();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 tmp = new Vector2();

    private final StringBuilder name = new StringBuilder();
    private float bgScroll = 0f, blink = 0f;
    private boolean confirmRequested = false, backRequested = false;

    // Campo de texto y botones (coords virtuales)
    private static final float FX = 60f, FY = 92f, FW = 200f, FH = 26f;
    private static final float BW = 96f, BH = 24f, BY = 48f;
    private static final float OKX = Constants.VIRTUAL_WIDTH / 2f + 6f;
    private static final float BACKX = Constants.VIRTUAL_WIDTH / 2f - BW - 6f;

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
        bgScroll += delta * 12f;
        blink += delta;

        Gdx.gl.glClearColor(0.05f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        bg.render(batch, 1, bgScroll);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.5f);
        shapes.rect(0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        // Campo de texto
        shapes.setColor(0.12f, 0.14f, 0.17f, 0.95f);
        shapes.rect(FX, FY, FW, FH);
        // Botones
        shapes.setColor(0.85f, 0.65f, 0.20f, 0.95f); shapes.rect(OKX, BY, BW, BH);   // CONFIRMAR
        shapes.setColor(0.30f, 0.32f, 0.36f, 0.95f); shapes.rect(BACKX, BY, BW, BH); // VOLVER
        shapes.end();
        // Borde del campo
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.85f, 0.85f, 0.85f, 1f);
        shapes.rect(FX, FY, FW, FH);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(1.2f);
        drawCentered("INGRESA TU NOMBRE, GUERRERO", Constants.VIRTUAL_HEIGHT - 26, new Color(0.95f, 0.85f, 0.45f, 1f));
        font.getData().setScale(1f);
        String shown = name.toString() + (((int) (blink * 2) % 2 == 0) ? "_" : " ");
        if (name.length() == 0 && (int) (blink * 2) % 2 != 0) shown = "";
        layout.setText(font, name.length() == 0 ? "Kayab" : shown);
        font.setColor(name.length() == 0 ? new Color(0.6f, 0.6f, 0.6f, 1f) : Color.WHITE);
        font.draw(batch, layout, FX + 8, FY + FH / 2f + 5f);
        drawCenteredIn("CONFIRMAR", OKX, BW, BY + BH / 2f + 4f, new Color(0.1f, 0.08f, 0.04f, 1f));
        drawCenteredIn("VOLVER", BACKX, BW, BY + BH / 2f + 4f, Color.WHITE);
        font.getData().setScale(0.8f);
        drawCentered("Escribe en PC, o toca el campo en el celular", 18, new Color(0.8f, 0.8f, 0.8f, 1f));
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
        if (inside(tmp.x, tmp.y, FX, FY, FW, FH)) openSoftKeyboard();
        else if (inside(tmp.x, tmp.y, OKX, BY, BW, BH)) confirmRequested = true;
        else if (inside(tmp.x, tmp.y, BACKX, BY, BW, BH)) backRequested = true;
    }

    private boolean inside(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    private void drawCentered(String text, float topY, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        font.draw(batch, layout, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, topY);
    }

    private void drawCenteredIn(String text, float x, float w, float topY, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        font.draw(batch, layout, x + (w - layout.width) / 2f, topY);
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(typing); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
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
