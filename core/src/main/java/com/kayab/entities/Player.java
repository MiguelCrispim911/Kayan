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

    public void update(boolean moveLeft, boolean moveRight, boolean jump, float cameraLeftEdge) {
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
    }

    public void renderDebug(ShapeRenderer sr) {
        Vector2 pos = body.getPosition();
        float px = pos.x * Constants.PPM;
        float py = pos.y * Constants.PPM;

        sr.setColor(Color.BLUE);
        // Rectángulo 16x32 centrado
        sr.rect(px - 8, py - 16, 16, 32);

        // Línea de dirección (TDD)
        sr.setColor(Color.WHITE);
        float lineDir = facingRight ? 8 : -8;
        sr.line(px, py + 8, px + lineDir, py + 8);
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }
}
