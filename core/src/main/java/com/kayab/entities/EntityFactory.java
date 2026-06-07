package com.kayab.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.kayab.Constants;

public class EntityFactory {

    public static Body createPlatform(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);

        Body body = world.createBody(bodyDef);
        body.setUserData("platform");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / Constants.PPM, height / 2f / Constants.PPM);
        body.createFixture(shape, 0.0f);
        shape.dispose();

        return body;
    }

    public static Body createArrowBody(World world, float x, float y, boolean facingRight) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.bullet = true;
        bodyDef.gravityScale = 0;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(3f / Constants.PPM, 1f / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        float velX = facingRight ? Constants.ARROW_SPEED_MPS : -Constants.ARROW_SPEED_MPS;
        body.setLinearVelocity(velX, 0);

        return body;
    }

    public static Body createEnemyBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        // TDD: 14x24 px -> 7x12 half
        shape.setAsBox(7f / Constants.PPM, 12f / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
}
