package com.kayab.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.kayab.Constants;

public class MovingPlatform {
    private Body body;
    private Vector2 startPos;
    private Vector2 endPos;
    private float speed;
    private boolean goingToEnd = true;

    public MovingPlatform(Body body, Vector2 startPos, Vector2 endPos, float speed) {
        this.body = body;
        this.startPos = startPos.scl(1f / Constants.PPM);
        this.endPos = endPos.scl(1f / Constants.PPM);
        this.speed = speed;
        this.body.setUserData("platform"); // Para que el jugador pueda saltar desde ella
    }

    public void update(float delta) {
        Vector2 currentPos = body.getPosition();
        Vector2 target = goingToEnd ? endPos : startPos;

        Vector2 direction = new Vector2(target).sub(currentPos);
        float distance = direction.len();

        if (distance < 0.1f) {
            goingToEnd = !goingToEnd;
        } else {
            direction.nor().scl(speed);
            body.setLinearVelocity(direction);
        }
    }

    public Body getBody() { return body; }
}
