package com.kayab.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class PreferencesDatabase implements IDatabase {
    private static final String PREFS_NAME = "kayab_saves";
    private Preferences prefs;
    private Json json;

    public PreferencesDatabase() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        json = new Json();
    }

    @Override
    public void createSave(SaveData data) {
        // En Preferences, simplificamos: solo guardamos uno por ahora o una lista JSON
        updateSave(data);
    }

    @Override
    public void updateSave(SaveData data) {
        String jsonData = json.toJson(data);
        prefs.putString("latest", jsonData);
        prefs.flush();
    }

    @Override
    public SaveData loadLatestSave() {
        String data = prefs.getString("latest", null);
        if (data == null) return null;
        return json.fromJson(SaveData.class, data);
    }

    @Override
    public Array<SaveData> loadAllSaves() {
        Array<SaveData> saves = new Array<>();
        SaveData latest = loadLatestSave();
        if (latest != null) saves.add(latest);
        return saves;
    }
}
