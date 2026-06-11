package com.kayab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.gfx.Background;

/**
 * US16 — Cinemáticas.
 * Reproduce una secuencia de escenas (fondo del mundo + caja de diálogo
 * inferior con personaje y texto multilínea). Se avanza tocando/Enter, con
 * un fade de entrada por escena. Al terminar, ejecuta el callback onComplete.
 * Como es una pantalla aparte, el input del juego queda pausado.
 */
public class CinematicScreen implements Screen {

    /** Una escena: fondo (mundo 1-4, o 0 = negro), quién habla, color y texto. */
    public static class Scene {
        public final int world;
        public final String speaker;
        public final Color color;
        public final String text;
        public Scene(int world, String speaker, Color color, String text) {
            this.world = world; this.speaker = speaker; this.color = color; this.text = text;
        }
    }

    private static final float FADE = 0.4f;

    private final KayabGame game;
    private final Scene[] scenes;
    private final Runnable onComplete;

    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final Background bg = new Background();
    private final Texture white;

    private int index = 0;
    private float sceneTime = 0f, scroll = 0f, blink = 0f;
    private boolean done = false;

    public CinematicScreen(KayabGame game, Scene[] scenes, Runnable onComplete) {
        this.game = game;
        this.scenes = scenes;
        this.onComplete = onComplete;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        white = new Texture(pm); pm.dispose();
    }

    @Override
    public void render(float delta) {
        if (done) return;
        if (scenes == null || scenes.length == 0) { finish(); return; }

        sceneTime += delta; scroll += delta * 8f; blink += delta;
        Scene s = scenes[index];
        float fadeT = Math.min(1f, sceneTime / FADE);

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        if (s.world >= 1) bg.render(batch, s.world, scroll);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Caja de diálogo inferior
        float bx = 8, by = 8, bw = Constants.VIRTUAL_WIDTH - 16, bh = 60;
        batch.setColor(0f, 0f, 0f, 0.72f);
        batch.draw(white, bx, by, bw, bh);
        batch.setColor(s.color.r, s.color.g, s.color.b, 0.9f);
        batch.draw(white, bx, by + bh - 2, bw, 2); // filete superior del color del personaje
        batch.setColor(Color.WHITE);

        // Personaje
        float textTop = by + bh - 8;
        if (s.speaker != null && !s.speaker.isEmpty()) {
            font.getData().setScale(1f);
            font.setColor(s.color);
            font.draw(batch, s.speaker, bx + 8, by + bh - 6);
            textTop = by + bh - 22;
        }
        // Texto (multilínea con ajuste)
        font.getData().setScale(0.9f);
        font.setColor(Color.WHITE);
        font.draw(batch, s.text, bx + 8, textTop, bw - 16, Align.left, true);

        // Pista de avance (parpadea)
        if ((int) (blink * 2) % 2 == 0) {
            font.getData().setScale(0.8f);
            font.setColor(0.85f, 0.85f, 0.85f, 1f);
            font.draw(batch, "toca para continuar  ▸", bx, by + 8, bw - 8, Align.right, false);
        }
        font.getData().setScale(1f);

        // Fade de entrada (negro -> transparente)
        if (fadeT < 1f) {
            batch.setColor(0f, 0f, 0f, 1f - fadeT);
            batch.draw(white, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
            batch.setColor(Color.WHITE);
        }
        batch.end();

        handleInput(fadeT);
    }

    private void handleInput(float fadeT) {
        boolean advance = Gdx.input.justTouched()
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        boolean skipAll = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        if (skipAll) { finish(); return; }
        if (!advance) return;

        if (fadeT < 1f) { sceneTime = FADE; return; } // primer toque: completar el fade
        index++;
        sceneTime = 0f;
        if (index >= scenes.length) finish();
    }

    private void finish() {
        if (done) return;
        done = true;
        onComplete.run();
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
        white.dispose();
    }
}
