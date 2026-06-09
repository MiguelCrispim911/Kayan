package com.kayab.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.badlogic.gdx.utils.Array;
import com.kayab.Constants;
import com.kayab.persistence.IDatabase;
import com.kayab.persistence.SaveData;

public class AndroidDatabase extends SQLiteOpenHelper implements IDatabase {

    private static final String TABLE_SAVES = "saves";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "player_name";
    private static final String COLUMN_WORLD = "current_world";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_HP = "hp";
    private static final String COLUMN_WORLDS_COMPLETE = "worlds_complete";
    private static final String COLUMN_LAST_SAVED = "last_saved";

    public AndroidDatabase(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SAVES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_WORLD + " INTEGER, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_HP + " INTEGER, " +
                COLUMN_WORLDS_COMPLETE + " TEXT, " +
                COLUMN_LAST_SAVED + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVES);
        onCreate(db);
    }

    @Override
    public void createSave(SaveData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, data.playerName);
        values.put(COLUMN_WORLD, data.currentWorld);
        values.put(COLUMN_SCORE, data.score);
        values.put(COLUMN_HP, data.hp);
        values.put(COLUMN_WORLDS_COMPLETE, data.worldsComplete);
        values.put(COLUMN_LAST_SAVED, String.valueOf(System.currentTimeMillis()));
        db.insert(TABLE_SAVES, null, values);
    }

    @Override
    public void updateSave(SaveData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORLD, data.currentWorld);
        values.put(COLUMN_SCORE, data.score);
        values.put(COLUMN_HP, data.hp);
        values.put(COLUMN_WORLDS_COMPLETE, data.worldsComplete);
        values.put(COLUMN_LAST_SAVED, String.valueOf(System.currentTimeMillis()));
        db.update(TABLE_SAVES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(data.id)});
    }

    @Override
    public SaveData loadLatestSave() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVES, null, null, null, null, null, COLUMN_ID + " DESC", "1");
        if (cursor != null && cursor.moveToFirst()) {
            SaveData data = cursorToSaveData(cursor);
            cursor.close();
            return data;
        }
        return null;
    }

    @Override
    public Array<SaveData> loadAllSaves() {
        Array<SaveData> saves = new Array<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVES, null, null, null, null, null, COLUMN_ID + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                saves.add(cursorToSaveData(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return saves;
    }

    private SaveData cursorToSaveData(Cursor cursor) {
        SaveData data = new SaveData();
        data.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        data.playerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
        data.currentWorld = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORLD));
        data.score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
        data.hp = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HP));
        data.worldsComplete = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORLDS_COMPLETE));
        data.lastSaved = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SAVED));
        return data;
    }
}
