package com.kayab.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.kayab.Constants;
import com.kayab.gfx.GameArt;

public class Arrow {
    private Body body;
    private float startX;
    private float maxRange;
    private boolean isPlayerArrow;
    private boolean active = true;

    public Arrow(Body body, float maxRange, boolean isPlayerArrow) {
        this.body = body;
        this.startX = body.getPosition().x;
        this.maxRange = maxRange;
        this.isPlayerArrow = isPlayerArrow;
        this.body.setUserData(this);
    }

    public void update() {
        if (!active) return;

        float distanceTraveled = Math.abs(body.getPosition().x - startX);
        if (distanceTraveled >= maxRange) {
            active = false;
        }
    }

    public void render(ShapeRenderer sr) {
        if (!active) return;

        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        sr.setColor(isPlayerArrow ? Color.YELLOW : Color.ORANGE);
        // Flecha de 6x2 px centrada
        sr.rect(px - 3, py - 1, 6, 2);
    }

    /** US-12: render con sprite (Fase 2). */
    public void renderSprite(SpriteBatch batch, GameArt art) {
        if (!active) return;
        Vector2 pos = body.getPosition();
        boolean right = body.getLinearVelocity().x >= 0;
        art.draw(batch, art.getArrow(isPlayerArrow),
                 pos.x * Constants.PPM, pos.y * Constants.PPM, right);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public Body getBody() {
        return body;
    }

    public boolean isPlayerArrow() {
        return isPlayerArrow;
    }
}
