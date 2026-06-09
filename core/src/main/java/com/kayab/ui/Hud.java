package com.kayab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.Constants;

public class Hud {
    private Viewport viewport;
    private OrthographicCamera camera;
    private BitmapFont font;
    private SpriteBatch batch;

    public Hud() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(0.6f);
    }

    public void render(ShapeRenderer sr, int playerHp, int score, String playerName) {
        // Asegurarnos de que el Viewport del HUD esté activo
        viewport.apply();

        // 1. Dibujar Corazones (Vida)
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.RED);
        for (int i = 0; i < playerHp; i++) {
            sr.rect(10 + (i * 12), Constants.VIRTUAL_HEIGHT - 15, 8, 8);
        }
        sr.end();

        // 2. Dibujar Texto
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, playerName.toUpperCase(), 10, Constants.VIRTUAL_HEIGHT - 18);
        font.draw(batch, "SCORE: " + score, Constants.VIRTUAL_WIDTH - 70, Constants.VIRTUAL_HEIGHT - 7);
        batch.end();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
