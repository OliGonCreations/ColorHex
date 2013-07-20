package com.jonas.colorhex;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "ColorHex";
    private final static int DATABASE_VERSION = 3;

    private final static String NAME_FAVORITE = "favorite";
    private final static String NAME_RECENT = "recent";
    private final static String KEY_ID = "id";
    private final static String KEY_COLOR = "color";
    private final static String KEY_HSV = "hsv";
    private final static String KEY_HEX = "hex";
    private final static String KEY_HEXA = "hexa";
    private final static String KEY_DEC = "dec";
    private final static String KEY_DECA = "deca";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITE = "CREATE TABLE " + NAME_FAVORITE + "(" + KEY_ID + " INTEGER," + KEY_COLOR + " TEXT PRIMARY KEY," + KEY_HSV + " TEXT," + KEY_HEX + " TEXT," + KEY_HEXA + " TEXT," + KEY_DEC + " TEXT," + KEY_DECA + " TEXT)";
        String CREATE_RECENT = "CREATE TABLE " + NAME_RECENT + "(" + KEY_ID + " INTEGER," + KEY_COLOR + " TEXT PRIMARY KEY," + KEY_HSV + " TEXT," + KEY_HEX + " TEXT," + KEY_HEXA + " TEXT," + KEY_DEC + " TEXT," + KEY_DECA + " TEXT)";
        db.execSQL(CREATE_FAVORITE);
        db.execSQL(CREATE_RECENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME_FAVORITE);
        db.execSQL("DROP TABLE IF EXISTS " + NAME_RECENT);
        onCreate(db);
    }

    public void addFavorite(int COLOR, String HSV, String HEX, String HEXA, String DEC, String DECA) {
        ContentValues values = new ContentValues();
        values.put(KEY_COLOR, COLOR);
        values.put(KEY_HSV, HSV);
        values.put(KEY_HEX, HEX);
        values.put(KEY_HEXA, HEXA);
        values.put(KEY_DEC, DEC);
        values.put(KEY_DECA, DECA);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(NAME_FAVORITE, null, values);
        db.close();
    }

    public void addRecent(int COLOR, String HSV, String HEX, String HEXA, String DEC, String DECA) {
        ContentValues values = new ContentValues();
        values.put(KEY_COLOR, COLOR);
        values.put(KEY_HSV, HSV);
        values.put(KEY_HEX, HEX);
        values.put(KEY_HEXA, HEXA);
        values.put(KEY_DEC, DEC);
        values.put(KEY_DECA, DECA);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(NAME_RECENT, null, values);
        db.close();
    }

    public List<Integer> getAllFavColors(boolean sort) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> colors = new ArrayList<Integer>();
        String query = "SELECT * FROM " + NAME_FAVORITE;
        if (sort)
            query = "SELECT * FROM " + NAME_FAVORITE + " ORDER BY " + KEY_HSV;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                colors.add(Integer.parseInt(cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        Collections.reverse(colors);
        return colors;
    }

    public List<Integer> getAllRecColors(boolean sort) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> colors = new ArrayList<Integer>();
        String query = "SELECT * FROM " + NAME_RECENT;
        if (sort)
            query = "SELECT * FROM " + NAME_RECENT + " ORDER BY " + KEY_HSV;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                colors.add(Integer.parseInt(cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        Collections.reverse(colors);
        return colors;
    }

    public List<String> getAllFavValues(int type, boolean sort) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> values = new ArrayList<String>();
        String query = "SELECT * FROM " + NAME_FAVORITE;
        if (sort)
            query = "SELECT * FROM " + NAME_FAVORITE + " ORDER BY " + KEY_HSV;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                values.add(cursor.getString(type + 2));
            } while (cursor.moveToNext());
        }
        Collections.reverse(values);
        return values;
    }

    public List<String> getAllRecValues(int type, boolean sort) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> values = new ArrayList<String>();
        String query = "SELECT * FROM " + NAME_RECENT;
        if (sort)
            query = "SELECT * FROM " + NAME_RECENT + " ORDER BY " + KEY_HSV;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                values.add(cursor.getString(type + 2));
            } while (cursor.moveToNext());
        }
        Collections.reverse(values);
        return values;
    }

    public void deleteFavorite(Integer color) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NAME_FAVORITE, KEY_COLOR + "=" + color, null);
        db.close();
    }

    public void deleteRecent(Integer color) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NAME_RECENT, KEY_COLOR + "=" + color, null);
        db.close();
    }


    public void deleteFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NAME_FAVORITE, null, null);
        db.close();
    }

    public void deleteRecents() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NAME_RECENT, null, null);
        db.close();
    }
}