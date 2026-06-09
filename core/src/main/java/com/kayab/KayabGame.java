package com.kayab;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.kayab.persistence.IDatabase;
import com.kayab.persistence.PreferencesDatabase;
import com.kayab.persistence.SaveData;

public class KayabGame extends Game {
    private IDatabase database;

    public KayabGame(IDatabase database) {
        this.database = database;
    }

    public KayabGame() {
    }

    @Override
    public void create() {
        if (database == null) {
            database = new PreferencesDatabase();
        }
        // US17: el juego abre en el menú principal
        setScreen(new MenuScreen(this));
    }

    /** Cambia de pantalla liberando la anterior (evita fugas al navegar). */
    public void changeScreen(Screen next) {
        Screen old = getScreen();
        setScreen(next);
        if (old != null) old.dispose();
    }

    public void showMenu() {
        changeScreen(new MenuScreen(this));
    }

    /** Nueva partida: crea el save con el nombre y arranca en el Mundo 1. */
    public void startNewGame(String playerName) {
        SaveData data = new SaveData();
        data.playerName = (playerName == null || playerName.trim().isEmpty()) ? "Kayab" : playerName.trim();
        data.currentWorld = 1;
        data.score = 0;
        data.hp = 3;
        data.worldsComplete = "";
        database.createSave(data);
        changeScreen(new GameScreen(this, data));
    }

    /** Continuar: retoma el save guardado en su mundo actual. */
    public void continueGame() {
        SaveData data = database.loadLatestSave();
        if (data == null) {
            data = new SaveData();
            database.createSave(data);
        }
        changeScreen(new GameScreen(this, data));
    }

    public IDatabase getDatabase() {
        return database;
    }
}
