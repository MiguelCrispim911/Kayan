package com.kayab.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.kayab.Constants;
import com.kayab.gfx.GameArt;

public class Boss {
    public enum Type { XOCOTL, VARGAS }

    private Body body;
    private Type type;
    private int maxHp;
    private int hp;
    private int phase = 1;
    private boolean active = true;
    private boolean facingRight = false;
    private float attackTimer = 0;
    private boolean shouldShoot = false;
    private float animTime = 0f; // US-12

    public Boss(Body body, Type type) {
        this.body = body;
        this.type = type;
        if (type == Type.XOCOTL) {
            this.maxHp = 10;
        } else {
            this.maxHp = 15;
        }
        this.hp = maxHp;
        this.body.setUserData(this);
    }

    public void update(float delta, Vector2 playerPos) {
        if (!active) return;
        animTime += delta;

        float currentX = body.getPosition().x;
        facingRight = playerPos.x > currentX;

        // Lógica de fases
        if (hp <= maxHp / 2 && phase == 1) {
            phase = 2;
            // Incrementar agresividad o cambiar patrón
        }

        attackTimer += delta;
        float attackCooldown = (phase == 1) ? 3.0f : 1.5f;

        if (attackTimer >= attackCooldown) {
            attackTimer = 0;
            shouldShoot = true;
        }

        // Movimiento básico (el Boss Xocotl se queda quieto en Fase 1, Vargas se mueve un poco)
        if (type == Type.VARGAS || phase == 2) {
            float speed = (phase == 1) ? 1f : 2f;
            float velX = facingRight ? speed : -speed;
            body.setLinearVelocity(velX, body.getLinearVelocity().y);
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
    }

    public boolean takeShootRequest() {
        if (shouldShoot) {
            shouldShoot = false;
            return true;
        }
        return false;
    }

    public void render(ShapeRenderer sr) {
        if (!active) return;
        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        // Colores y tamaños según TDD
        if (type == Type.XOCOTL) {
            sr.setColor(new Color(0.6f, 0, 0, 1)); // Rojo oscuro
            sr.rect(px - 14, py - 20, 28, 40);
        } else {
            sr.setColor(Color.DARK_GRAY);
            sr.rect(px - 10, py - 19, 20, 38);
        }

        // Barra de HP (US11)
        sr.setColor(Color.BLACK);
        sr.rect(px - 15, py + 25, 30, 4);
        sr.setColor(Color.GREEN);
        sr.rect(px - 15, py + 25, 30 * ((float)hp / maxHp), 4);
    }

    /** US-12: render con sprites (Fase 2) + barra de HP. Misma física. */
    public void renderSprite(SpriteBatch batch, GameArt art) {
        if (!active) return;
        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        art.draw(batch, art.bossFrame(type, animTime), px, py, facingRight);

        // Barra de HP (US11) usando textura blanca tintada
        TextureRegion w = art.getWhite();
        batch.setColor(Color.BLACK); batch.draw(w, px - 15, py + 25, 30, 4);
        batch.setColor(Color.GREEN); batch.draw(w, px - 15, py + 25, 30 * ((float) hp / maxHp), 4);
        batch.setColor(Color.WHITE);
    }

    public void hit() {
        hp--;
        if (hp <= 0) active = false;
    }

    public boolean isActive() { return active; }
    public Body getBody() { return body; }
    public boolean isFacingRight() { return facingRight; }
}
