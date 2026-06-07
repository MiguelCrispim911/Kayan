package com.kayab;

public class Constants {
    public static final float PPM = 32f;   // pixels per meter
    public static final float PLAYER_SPEED = 5f;    // metros/seg (= 160px/seg)
    public static final float JUMP_IMPULSE = 8f;    // metros/seg impulso (un solo salto)
    public static final float ARROW_SPEED_MPS = 15f;   // metros/seg (= 480px/seg)
    public static final float ARROW_MAX_RANGE = 6.25f; // metros (= 200px) — JUGADOR
    public static final float ENEMY_ARROW_RANGE = 4.7f;  // metros (= 150px) — ENEMIGOS
    public static final float ARROW_COOLDOWN = 0.4f;  // segundos entre flechas

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
