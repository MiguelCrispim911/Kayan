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
        // En Preferences guardamos una lista JSON de saves
        if (data == null) return;
        String raw = prefs.getString("saves", "[]");
        Array<SaveData> saves = new Array<>();
        try {
            SaveData[] arr = json.fromJson(SaveData[].class, raw);
            if (arr != null) for (SaveData s : arr) saves.add(s);
        } catch (Exception e) { /* ignore, start fresh */ }

        // Assign an id if none
        if (data.id == 0) data.id = (int) (System.currentTimeMillis() & 0x7fffffff);
        data.lastSaved = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        saves.add(data);
        prefs.putString("saves", json.toJson(saves.items));
        prefs.flush();
    }

    @Override
    public void updateSave(SaveData data) {
        if (data == null) return;
        String raw = prefs.getString("saves", "[]");
        Array<SaveData> saves = new Array<>();
        try {
            SaveData[] arr = json.fromJson(SaveData[].class, raw);
            if (arr != null) for (SaveData s : arr) saves.add(s);
        } catch (Exception e) { /* ignore */ }

        boolean found = false;
        for (int i = 0; i < saves.size; i++) {
            if (saves.get(i).id == data.id) {
                saves.set(i, data);
                found = true;
                break;
            }
        }
        if (!found) {
            if (data.id == 0) data.id = (int) (System.currentTimeMillis() & 0x7fffffff);
            saves.add(data);
        }
        data.lastSaved = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        prefs.putString("saves", json.toJson(saves.items));
        prefs.flush();
    }

    @Override
    public SaveData loadLatestSave() {
        Array<SaveData> saves = loadAllSaves();
        if (saves == null || saves.size == 0) return null;
        // Devolver el último guardado (más reciente)
        return saves.get(saves.size - 1);
    }

    @Override
    public Array<SaveData> loadAllSaves() {
        Array<SaveData> saves = new Array<>();
        String raw = prefs.getString("saves", null);
        if (raw == null) return saves;
        try {
            SaveData[] arr = json.fromJson(SaveData[].class, raw);
            if (arr != null) for (SaveData s : arr) saves.add(s);
        } catch (Exception e) { /* malformed, return empty */ }
        return saves;
    }
}
