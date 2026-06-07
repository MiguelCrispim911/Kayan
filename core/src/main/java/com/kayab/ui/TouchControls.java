package com.kayab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayab.Constants;

public class TouchControls {
    private Viewport uiViewport;
    private OrthographicCamera uiCamera;

    // Joystick
    private Vector2 basePos = new Vector2(60, 60);
    private Vector2 knobPos = new Vector2(60, 60);
    private final float BASE_RADIUS = 40f;
    private final float KNOB_RADIUS = 15f;

    // Botón Disparo (rectángulo 70x70px según TDD)
    private float buttonX = Constants.VIRTUAL_WIDTH - 60;
    private float buttonY = 60;
    private float buttonSize = 70;

    public boolean moveLeft, moveRight, jumpRequest, fireHeld;
    private boolean wasJumpingLastFrame = false;

    public TouchControls() {
        // Fix 3: Usamos una cámara explícita y centrada para evitar problemas de coordenadas iniciales
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        this.uiViewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, uiCamera);
    }

    public void update() {
        moveLeft = false;
        moveRight = false;
        jumpRequest = false;
        fireHeld = false;
        boolean isJumpJoystickHeld = false;
        knobPos.set(basePos);

        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector2 touch = uiViewport.unproject(new Vector2(Gdx.input.getX(i), Gdx.input.getY(i)));

                // Lógica Joystick
                float dist = touch.dst(basePos);
                if (dist < BASE_RADIUS * 2) {
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
                    if (dy > 20) isJumpJoystickHeld = true; // Joystick hacia arriba
                }

                // Lógica Botón Disparo (Esquina derecha)
                if (touch.x > buttonX - buttonSize/2 && touch.x < buttonX + buttonSize/2 &&
                    touch.y > buttonY - buttonSize/2 && touch.y < buttonY + buttonSize/2) {
                    fireHeld = true;
                }
            }
        }

        // Fix 1: Flanco de subida para el salto (evita salto infinito al mantener arriba)
        if (isJumpJoystickHeld && !wasJumpingLastFrame) {
            jumpRequest = true;
        }
        wasJumpingLastFrame = isJumpJoystickHeld;
    }

    public void render(ShapeRenderer sr) {
        // Fix 2: Habilitar Blending explícitamente para que el alpha funcione
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(uiViewport.getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Joystick Base (Blanco semitransparente 30%)
        sr.setColor(1, 1, 1, 0.3f);
        sr.circle(basePos.x, basePos.y, BASE_RADIUS);

        // Joystick Knob (Blanco semitransparente 60%)
        sr.setColor(1, 1, 1, 0.6f);
        sr.circle(knobPos.x, knobPos.y, KNOB_RADIUS);

        // Botón Disparo (Rectángulo semitransparente 40%)
        sr.setColor(1, 1, 1, 0.4f);
        sr.rect(buttonX - buttonSize/2, buttonY - buttonSize/2, buttonSize, buttonSize);

        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }
}
