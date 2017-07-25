package com.example.adriano.hotspotfinder;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBClass extends SQLiteOpenHelper {

    public static final String TABLE_WIFI = "wifi";
    public static final String COLUMN_SSID = "ssid";
    public static final String COLUMN_BSSID = "bssid";
    public static final String COLUMN_CAP = "capabilities";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    private static final String DATABASE_NAME = "wifi_list.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_WIFI + "( "
            + COLUMN_SSID + " text not null, "
            + COLUMN_BSSID + " text not null, "
            + COLUMN_CAP + " text not null, "
            + COLUMN_LEVEL + " integer not null, "
            + COLUMN_FREQUENCY + " integer not null, "
            + COLUMN_LATITUDE + " real not null, "
            + COLUMN_LONGITUDE + " real not null, "
            + "PRIMARY KEY (" + COLUMN_SSID + ", " + COLUMN_BSSID  +") );";


    public DBClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);
        onCreate(db);
    }

    public long insertNewConnection(String ssid, String bssid, String capabilities, int level, int frequency, double latitude, double longitude) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SSID, ssid);
        cv.put(COLUMN_BSSID, bssid);
        cv.put(COLUMN_CAP, capabilities);
        cv.put(COLUMN_LEVEL, level);
        cv.put(COLUMN_FREQUENCY, frequency);
        cv.put(COLUMN_LATITUDE, latitude);
        cv.put(COLUMN_LONGITUDE, longitude);

        long code = getWritableDatabase().insert(TABLE_WIFI, null, cv);
        return code;
    }

    public void reset(){
        getWritableDatabase().execSQL("delete from "+ TABLE_WIFI);
    }

    public Cursor getData() {
        return getWritableDatabase().query(TABLE_WIFI, null, null, null, null, null, null);
    }
}
