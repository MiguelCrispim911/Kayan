package com.kayab.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.kayab.Constants;
import com.kayab.gfx.GameArt;

public class Enemy {
    public enum Type { BASIC, SHIELD, SWORD, GUN }

    private Body body;
    private Type type;
    private int hp;
    private boolean facingRight = false;
    private boolean active = true;

    private float fireTimer = 0;
    private float patrolXStart;
    private float patrolRange;
    private boolean shouldShoot = false;
    private float animTime = 0f; // US-12

    public Enemy(Body body, Type type, float patrolRange) {
        this.body = body;
        this.type = type;
        this.patrolXStart = body.getPosition().x;
        this.patrolRange = patrolRange / Constants.PPM;

        switch (type) {
            case BASIC: this.hp = 2; break;
            case SHIELD: this.hp = 3; break;
            default: this.hp = 2; break;
        }

        this.body.setUserData(this);
    }

    public void update(float delta, Vector2 playerPos) {
        if (!active) return;
        animTime += delta;

        float currentX = body.getPosition().x;
        float distToPlayer = body.getPosition().dst(playerPos);

        // Lógica de detección y comportamiento diferenciado (GDD US07)
        if (distToPlayer < Constants.ENEMY_DETECTION_RANGE) {
            facingRight = playerPos.x > currentX;

            if (type == Type.SHIELD) {
                // SHIELD: Avanza lentamente hacia Kayab sin detenerse
                float slowSpeed = Constants.ENEMY_SPEED * 0.6f;
                float velX = facingRight ? slowSpeed : -slowSpeed;
                body.setLinearVelocity(velX, body.getLinearVelocity().y);
            } else {
                // BASIC: Se detiene
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            }

            // Ambos tipos disparan si están en rango (US07 adaptado)
            fireTimer += delta;
            if (fireTimer >= Constants.ENEMY_FIRE_RATE) {
                fireTimer = 0;
                shouldShoot = true;
            }
        } else {
            // Patrulla horizontal estándar cuando está fuera de rango
            shouldShoot = false;
            float velX = facingRight ? Constants.ENEMY_SPEED : -Constants.ENEMY_SPEED;
            body.setLinearVelocity(velX, body.getLinearVelocity().y);

            if (facingRight && currentX > patrolXStart + patrolRange) {
                facingRight = false;
            } else if (!facingRight && currentX < patrolXStart - patrolRange) {
                facingRight = true;
            }
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

        // Colores Fase 1 según TDD
        if (type == Type.SHIELD) {
            sr.setColor(Color.ORANGE);
        } else {
            sr.setColor(Color.RED);
        }

        // Rectángulo base del cuerpo (Igualado a 16x32 del héroe, ancho 14 para diferenciar un poco)
        sr.rect(px - 7, py - 16, 14, 32);

        // Detalle visual para el Guerrero Escudo (rectángulo dorado al frente)
        if (type == Type.SHIELD) {
            sr.setColor(Color.GOLD);
            float shieldOffset = facingRight ? 3 : -7;
            sr.rect(px + shieldOffset, py - 14, 4, 28);
        }

        // Línea de dirección (indica hacia dónde mira)
        sr.setColor(Color.WHITE);
        float dirLine = facingRight ? 7 : -7;
        sr.line(px, py, px + dirLine, py);
    }

    /** US-12: render con sprites (Fase 2). Mismo cuerpo Box2D, solo cambia el dibujo. */
    public void renderSprite(SpriteBatch batch, GameArt art) {
        if (!active) return;
        Vector2 pos = body.getPosition();
        boolean moving = Math.abs(body.getLinearVelocity().x) > 0.1f;
        art.draw(batch, art.enemyFrame(type, animTime, moving),
                 pos.x * Constants.PPM, pos.y * Constants.PPM, facingRight);
    }

    public void hit() {
        hp--;
        if (hp <= 0) active = false;
    }

    public boolean isActive() { return active; }
    public Body getBody() { return body; }
    public boolean isFacingRight() { return facingRight; }
    public Type getType() { return type; }
}
