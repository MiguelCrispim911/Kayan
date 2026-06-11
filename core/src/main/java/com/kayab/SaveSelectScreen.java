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
import com.kayab.persistence.SaveData;
import com.kayab.persistence.IDatabase;
import com.badlogic.gdx.utils.Array;

/** Pantalla simple para seleccionar una partida guardada. */
public class SaveSelectScreen implements Screen {

    private final KayabGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    private final GlyphLayout layout = new GlyphLayout();

    private Array<SaveData> saves;
    private int selected = 0;

    public SaveSelectScreen(KayabGame game) {
        this.game = game;
        IDatabase db = game.getDatabase();
        saves = db.loadAllSaves();
        if (saves == null) saves = new Array<>();
    }

    @Override public void show() { Gdx.input.setInputProcessor(null); }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        layout.setText(font, "Elige una partida");
        font.draw(batch, "Elige una partida", 12, Constants.VIRTUAL_HEIGHT - 10);

        font.getData().setScale(0.9f);
        for (int i = 0; i < saves.size; i++) {
            SaveData s = saves.get(i);
            String line = String.format(java.util.Locale.US, "%s  | cap: %d  pts: %d  vidas: %d  %s", s.playerName, s.currentWorld, s.score, s.hp, s.lastSaved == null ? "" : s.lastSaved);
            float y = Constants.VIRTUAL_HEIGHT - 36 - i * 18;
            if (i == selected) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + line, 8, y);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, line, 18, y);
            }
        }

        // Volver
        font.setColor(Color.WHITE);
        font.getData().setScale(0.8f);
        font.draw(batch, "(Esc) Volver", 8, 12);
        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selected = Math.max(0, selected - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected = Math.min(saves.size - 1, selected + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (saves.size > 0) {
                game.continueWithSave(saves.get(selected));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.showMenu();
        }
        if (Gdx.input.justTouched()) {
            Vector2 tmp = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(tmp);
            // compute clicked index
            float top = Constants.VIRTUAL_HEIGHT - 36;
            int idx = (int) ((top - tmp.y) / 18f);
            if (idx >= 0 && idx < saves.size) {
                game.continueWithSave(saves.get(idx));
            }
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() { batch.dispose(); font.dispose(); }
}

