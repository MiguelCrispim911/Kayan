package com.kayab;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.kayab.persistence.IDatabase;
import com.kayab.persistence.PreferencesDatabase;
import com.kayab.persistence.SaveData;
import com.kayab.screenshot.IScreenshot;

public class KayabGame extends Game {
    private IDatabase database;
    private IScreenshot screenshotService;
    private SaveData currentSave;

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

    /** Nueva partida: crea el save, intro y cinemática de apertura del Mundo 1. */
    public void startNewGame(String playerName) {
        currentSave = new SaveData();
        currentSave.playerName = (playerName == null || playerName.trim().isEmpty()) ? "Kayab" : playerName.trim();
        currentSave.currentWorld = 1;
        currentSave.score = 0;
        currentSave.hp = Constants.PLAYER_MAX_HP;
        currentSave.worldsComplete = "";
        database.createSave(currentSave);
        // US16: intro -> apertura del Mundo 1 -> nivel
        changeScreen(new CinematicScreen(this, Cinematics.intro(), () -> playOpening(1)));
    }

    /** Continuar: retoma el save guardado en su mundo actual (sin cinemática). */
    public void continueGame() {
        currentSave = database.loadLatestSave();
        if (currentSave == null) {
            currentSave = new SaveData();
            database.createSave(currentSave);
        }
        startWorld(currentSave.currentWorld);
    }

    /** Continuar con una partida concreta (elegida en el menú). */
    public void continueWithSave(com.kayab.persistence.SaveData data) {
        if (data == null) return;
        this.currentSave = data;
        startWorld(currentSave.currentWorld);
    }

    /** US16: cinemática de apertura del mundo, luego el nivel. */
    public void playOpening(int world) {
        currentSave.currentWorld = world;
        database.updateSave(currentSave);
        changeScreen(new CinematicScreen(this, Cinematics.opening(world), () -> startWorld(world)));
    }

    /** Arranca el nivel del mundo indicado. */
    public void startWorld(int world) {
        currentSave.currentWorld = world;
        changeScreen(new GameScreen(this, currentSave));
    }

    /** US16: el nivel terminó -> cierre del mundo -> apertura del siguiente,
     *  o final + epílogo tras el Mundo 4. */
    public void onWorldCleared() {
        int w = currentSave.currentWorld;
        if (w < 4) {
            changeScreen(new CinematicScreen(this, Cinematics.closing(w), () -> playOpening(w + 1)));
        } else {
            changeScreen(new CinematicScreen(this, Cinematics.ending(), this::showMenu));
        }
    }

    public IDatabase getDatabase() {
        return database;
    }

    public void setScreenshotService(IScreenshot service) {
        this.screenshotService = service;
    }

    public IScreenshot getScreenshotService() {
        return screenshotService;
    }
}
