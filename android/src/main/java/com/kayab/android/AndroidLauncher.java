package com.kayab.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.kayab.KayabGame;
import com.kayab.screenshot.AndroidScreenshot;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        // Inyectamos la base de datos de Android (SQLite)
        AndroidDatabase database = new AndroidDatabase(this);
        KayabGame game = new KayabGame(database);
        game.setScreenshotService(new AndroidScreenshot(this));
        initialize(game, configuration);
    }
}
