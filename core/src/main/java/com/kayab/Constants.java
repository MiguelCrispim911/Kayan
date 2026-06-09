package com.kayab;

public class Constants {
    public static final float PPM = 32f;   // pixels per meter
    public static final float PLAYER_SPEED = 3.2f;
    public static final float JUMP_IMPULSE = 4.2f; // Altura ~29px. Rango horizontal ~85px.
    public static final float ARROW_SPEED_MPS = 10f;
    public static final float ARROW_MAX_RANGE = 7.0f; // 224px
    public static final float ENEMY_ARROW_RANGE = 5.5f; // 176px
    public static final float ARROW_COOLDOWN = 0.4f;

    // Enemigos
    public static final float ENEMY_SPEED = 1.8f;
    public static final float ENEMY_DETECTION_RANGE = 5.5f;
    public static final float ENEMY_FIRE_RATE = 1.8f; // segundos

    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 180;
    public static final int TILE_SIZE = 16;
    public static final int PLAYER_MAX_HP = 3;

    // Puntaje
    public static final int SCORE_BASIC_ENEMY = 25;
    public static final int SCORE_SHIELD_ENEMY = 40;
    public static final int SCORE_SWORD_ENEMY = 30;
    public static final int SCORE_GUN_ENEMY = 50;
    public static final int SCORE_WORLD_CLEAR = 500;
    public static final int SCORE_BOSS_XOCOTL = 1000;
    public static final int SCORE_BOSS_VARGAS = 2000;

    // Base de datos
    public static final String DB_NAME = "kayab.db";
    public static final int DB_VERSION = 1;
}
