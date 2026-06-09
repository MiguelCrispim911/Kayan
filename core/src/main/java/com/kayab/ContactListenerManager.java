package com.kayab;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.kayab.entities.Arrow;
import com.kayab.entities.Boss;
import com.kayab.entities.Enemy;
import com.kayab.entities.Player;

public class ContactListenerManager implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        // Detección de suelo para el jugador
        if (isPlayerFoot(fa) && isPlatform(fb)) {
            updatePlayerGroundContacts(fa, 1);
        } else if (isPlayerFoot(fb) && isPlatform(fa)) {
            updatePlayerGroundContacts(fb, 1);
        }

        // Colisión de flechas (US06, US07, US08)
        checkArrowCollision(fa, fb);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (isPlayerFoot(fa) && isPlatform(fb)) {
            updatePlayerGroundContacts(fa, -1);
        } else if (isPlayerFoot(fb) && isPlatform(fa)) {
            updatePlayerGroundContacts(fb, -1);
        }
    }

    private boolean isPlayerFoot(Fixture f) {
        return f.getUserData() != null && f.getUserData().equals("foot");
    }

    private boolean isPlatform(Fixture f) {
        return f.getBody().getUserData() != null && f.getBody().getUserData().equals("platform");
    }

    private void updatePlayerGroundContacts(Fixture footFixture, int delta) {
        Object playerObj = footFixture.getBody().getUserData();
        if (playerObj instanceof Player) {
            ((Player) playerObj).changeGroundContacts(delta);
        }
    }

    private void checkArrowCollision(Fixture fa, Fixture fb) {
        Object ua = fa.getBody().getUserData();
        Object ub = fb.getBody().getUserData();

        if (ua instanceof Arrow) {
            handleArrowImpact((Arrow) ua, fb);
        } else if (ub instanceof Arrow) {
            handleArrowImpact((Arrow) ub, fa);
        }
    }

    private void handleArrowImpact(Arrow arrow, Fixture other) {
        Object otherData = other.getBody().getUserData();

        // Impacto con plataforma
        if ("platform".equals(otherData)) {
            arrow.deactivate();
        }

        // Impacto flecha JUGADOR -> ENEMIGO
        if (arrow.isPlayerArrow() && otherData instanceof Enemy) {
            ((Enemy) otherData).hit();
            arrow.deactivate();
        }

        // Impacto flecha JUGADOR -> BOSS
        if (arrow.isPlayerArrow() && otherData instanceof Boss) {
            ((Boss) otherData).hit();
            arrow.deactivate();
        }

        // Impacto flecha ENEMIGO -> JUGADOR (US08)
        if (!arrow.isPlayerArrow() && otherData instanceof Player) {
            ((Player) otherData).hit();
            arrow.deactivate();
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
