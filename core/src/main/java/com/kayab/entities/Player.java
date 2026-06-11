package com.kayab.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.kayab.gfx.GameArt;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.kayab.Constants;

public class Player {
    private Body body;
    private boolean facingRight = true;
    private int groundContacts = 0; // Contador para evitar el bug de las costuras
    private int hp = Constants.PLAYER_MAX_HP;
    private boolean alive = true;
    private float hitFlashTimer = 0;

    // US-12: estado de animación (no afecta física)
    private float animTime = 0f;
    private float shootTimer = 0f;
    private boolean moving = false;

    public Player(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX / Constants.PPM, startY / Constants.PPM);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / Constants.PPM, 16f / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        body.createFixture(fixtureDef);
        shape.dispose();

        PolygonShape feetSensorShape = new PolygonShape();
        feetSensorShape.setAsBox(6f / Constants.PPM, 2f / Constants.PPM,
                                 new Vector2(0, -16f / Constants.PPM), 0);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = feetSensorShape;
        sensorDef.isSensor = true;
        Fixture feetFixture = body.createFixture(sensorDef);
        feetFixture.setUserData("foot");
        feetSensorShape.dispose();

        body.setUserData(this);
    }

    public void update(float delta, boolean moveLeft, boolean moveRight, boolean jump, float cameraLeftEdge) {
        if (!alive) return;

        if (hitFlashTimer > 0) hitFlashTimer -= delta;
        animTime += delta;
        if (shootTimer > 0) shootTimer -= delta;
        moving = moveLeft || moveRight;

        Vector2 vel = body.getLinearVelocity();
        float targetX = 0;

        if (moveLeft) {
            targetX = -Constants.PLAYER_SPEED;
            facingRight = false;
        } else if (moveRight) {
            targetX = Constants.PLAYER_SPEED;
            facingRight = true;
        }

        body.setLinearVelocity(targetX, vel.y);

        // Saltamos si hay al menos un contacto con el suelo
        if (jump && groundContacts > 0) {
            body.setLinearVelocity(vel.x, 0); // Limpiar velocidad vertical para saltos consistentes
            body.applyLinearImpulse(new Vector2(0, Constants.JUMP_IMPULSE * body.getMass()), body.getWorldCenter(), true);
        }

        float playerHalfWidth = 8f / Constants.PPM;
        Vector2 pos = body.getPosition();
        if (pos.x - playerHalfWidth < cameraLeftEdge) {
            body.setTransform(cameraLeftEdge + playerHalfWidth, pos.y, 0);
            if (body.getLinearVelocity().x < 0) {
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
        }

        if (pos.y < -1f) {
            die();
        }
    }

    public void renderDebug(ShapeRenderer sr) {
        if (!alive) return;
        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        if (hitFlashTimer > 0 && (int)(hitFlashTimer * 10) % 2 == 0) {
            sr.setColor(Color.RED);
        } else {
            sr.setColor(Color.BLUE);
        }
        sr.rect(px - 8, py - 16, 16, 32);

        sr.setColor(Color.WHITE);
        float lineDir = facingRight ? 8 : -8;
        sr.line(px, py + 8, px + lineDir, py + 8);
    }

    /** US-12: render con sprites (Fase 2). Mismo cuerpo Box2D, solo cambia el dibujo. */
    public void renderSprite(SpriteBatch batch, GameArt art) {
        if (!alive) return;
        // Parpadeo al recibir daño: saltar el dibujo en frames alternos
        if (hitFlashTimer > 0 && (int) (hitFlashTimer * 10) % 2 == 0) return;
        Vector2 pos = body.getPosition();
        boolean shooting = shootTimer > 0;
        art.draw(batch, art.playerFrame(animTime, isOnGround(), moving, shooting),
                 pos.x * Constants.PPM, pos.y * Constants.PPM, facingRight);
    }

    /** Llamar al disparar para mostrar la animación de tiro. */
    public void onShoot() { shootTimer = 0.18f; }

    public boolean isOnGround() { return groundContacts > 0; }

    public void changeGroundContacts(int delta) {
        groundContacts += delta;
        if (groundContacts < 0) groundContacts = 0;
    }

    public void hit() {
        if (!alive) return;
        hp--;
        hitFlashTimer = 0.5f;
        if (hp <= 0) die();
    }

    private void die() { hp = 0; alive = false; }
    public Vector2 getPosition() { return body.getPosition(); }
    public boolean isFacingRight() { return facingRight; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, Constants.PLAYER_MAX_HP)); }
    public boolean isAlive() { return alive; }
}
