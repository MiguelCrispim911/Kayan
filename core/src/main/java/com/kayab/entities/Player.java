package com.kayab.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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
    private boolean onGround = false;
    private int hp = Constants.PLAYER_MAX_HP;
    private boolean alive = true;
    private float hitFlashTimer = 0;

    public Player(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX / Constants.PPM, startY / Constants.PPM);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        // Hitbox principal (16x32px)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / Constants.PPM, 16f / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        body.createFixture(fixtureDef);
        shape.dispose();

        // Sensor de pies para detección de suelo
        PolygonShape feetSensorShape = new PolygonShape();
        // Un rectángulo pequeño en la base
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

        if (jump && onGround) {
            body.applyLinearImpulse(new Vector2(0, Constants.JUMP_IMPULSE), body.getWorldCenter(), true);
            onGround = false;
        }

        // Lógica de pared invisible izquierda (TDD)
        float playerHalfWidth = 8f / Constants.PPM;
        Vector2 pos = body.getPosition();
        if (pos.x - playerHalfWidth < cameraLeftEdge) {
            body.setTransform(cameraLeftEdge + playerHalfWidth, pos.y, 0);
            if (body.getLinearVelocity().x < 0) {
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
        }

        // Muerte por caída (GDD: Caer a un hueco -> muerte instantánea)
        if (pos.y < -1f) {
            die();
        }
    }

    public void renderDebug(ShapeRenderer sr) {
        if (!alive) return;

        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        // Feedback visual de daño: parpadeo rojo
        if (hitFlashTimer > 0 && (int)(hitFlashTimer * 10) % 2 == 0) {
            sr.setColor(Color.RED);
        } else {
            sr.setColor(Color.BLUE);
        }

        // Rectángulo 16x32 centrado
        sr.rect(px - 8, py - 16, 16, 32);

        // Línea de dirección (TDD)
        sr.setColor(Color.WHITE);
        float lineDir = facingRight ? 8 : -8;
        sr.line(px, py + 8, px + lineDir, py + 8);
    }

    public void hit() {
        if (!alive) return;
        hp--;
        hitFlashTimer = 0.5f;
        if (hp <= 0) {
            die();
        }
    }

    private void die() {
        hp = 0;
        alive = false;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public int getHp() {
        return hp;
    }

    public boolean isAlive() {
        return alive;
    }
}
