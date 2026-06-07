package com.kayab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.Constants;

public class TouchControls {
    private Viewport viewport;

    // Joystick
    private Vector2 basePos = new Vector2(60, 60);
    private Vector2 knobPos = new Vector2(60, 60);
    private final float BASE_RADIUS = 40f;
    private final float KNOB_RADIUS = 15f;

    // Botón Salto/Disparo (En este prototipo, el joystick arriba salta, pero añadimos un botón)
    private float buttonX = Constants.VIRTUAL_WIDTH - 60;
    private float buttonY = 60;
    private float buttonSize = 50;

    public boolean moveLeft, moveRight, jumpRequest;

    public TouchControls(Viewport viewport) {
        this.viewport = viewport;
    }

    public void update() {
        moveLeft = false;
        moveRight = false;
        jumpRequest = false;
        knobPos.set(basePos);

        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(i), Gdx.input.getY(i)));

                // Lógica Joystick
                float dist = touch.dst(basePos);
                if (dist < BASE_RADIUS * 2) { // Área de activación un poco más grande
                    if (dist > BASE_RADIUS) {
                        float angle = (float) Math.atan2(touch.y - basePos.y, touch.x - basePos.x);
                        knobPos.set(basePos.x + (float)Math.cos(angle) * BASE_RADIUS,
                                    basePos.y + (float)Math.sin(angle) * BASE_RADIUS);
                    } else {
                        knobPos.set(touch);
                    }

                    float dx = knobPos.x - basePos.x;
                    float dy = knobPos.y - basePos.y;

                    if (dx < -15) moveLeft = true;
                    if (dx > 15) moveRight = true;
                    if (dy > 20) jumpRequest = true; // Joystick hacia arriba
                }

                // Lógica Botón (Esquina derecha)
                if (touch.x > buttonX - buttonSize/2 && touch.x < buttonX + buttonSize/2 &&
                    touch.y > buttonY - buttonSize/2 && touch.y < buttonY + buttonSize/2) {
                    jumpRequest = true;
                }
            }
        }
    }

    public void render(ShapeRenderer sr) {
        sr.setProjectionMatrix(viewport.getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Joystick Base
        sr.setColor(1, 1, 1, 0.3f);
        sr.circle(basePos.x, basePos.y, BASE_RADIUS);

        // Joystick Knob
        sr.setColor(1, 1, 1, 0.6f);
        sr.circle(knobPos.x, knobPos.y, KNOB_RADIUS);

        // Botón
        sr.setColor(1, 1, 1, 0.4f);
        sr.rect(buttonX - buttonSize/2, buttonY - buttonSize/2, buttonSize, buttonSize);

        sr.end();
    }
}
