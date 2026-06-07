package com.kayab;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class KayabGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
