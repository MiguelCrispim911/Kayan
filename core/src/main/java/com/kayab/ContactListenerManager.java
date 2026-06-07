package com.kayab;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.kayab.entities.Player;

public class ContactListenerManager implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        // Detección de suelo para el jugador
        if (fa.getUserData() != null && fa.getUserData().equals("foot")) {
            if (fb.getBody().getUserData() != null && fb.getBody().getUserData().equals("platform")) {
                Object playerObj = fa.getBody().getUserData();
                if (playerObj instanceof Player) {
                    ((Player) playerObj).setOnGround(true);
                }
            }
        }
        if (fb.getUserData() != null && fb.getUserData().equals("foot")) {
            if (fa.getBody().getUserData() != null && fa.getBody().getUserData().equals("platform")) {
                Object playerObj = fb.getBody().getUserData();
                if (playerObj instanceof Player) {
                    ((Player) playerObj).setOnGround(true);
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData() != null && fa.getUserData().equals("foot")) {
            Object playerObj = fa.getBody().getUserData();
            if (playerObj instanceof Player) {
                ((Player) playerObj).setOnGround(false);
            }
        }
        if (fb.getUserData() != null && fb.getUserData().equals("foot")) {
            Object playerObj = fb.getBody().getUserData();
            if (playerObj instanceof Player) {
                ((Player) playerObj).setOnGround(false);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
