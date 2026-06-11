package com.kayab.persistence;

public class SaveData {
    public int id;
    public String playerName;
    public int currentWorld;
    public int score;
    public int hp;
    public String worldsComplete;
    public String lastSaved;

    public SaveData() {
        this.playerName = "Kayab";
        this.currentWorld = 1;
        this.score = 0;
        this.hp = 8;
        this.worldsComplete = "";
    }
}
