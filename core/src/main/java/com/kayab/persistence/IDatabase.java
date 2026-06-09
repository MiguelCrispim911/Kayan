package com.kayab.persistence;

import com.badlogic.gdx.utils.Array;

public interface IDatabase {
    void createSave(SaveData data);
    void updateSave(SaveData data);
    SaveData loadLatestSave();
    Array<SaveData> loadAllSaves();
}
