package com.bhavaniprasad.downloadtask;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DatabaseManager  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Download_data2";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "download_datatable";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_FILE = "file";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_sizeInBytes = "sizeInBytes";
    private static final String COLUMN_cdn_path = "cdn_path";

    DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        String sql = "CREATE TABLE " + TABLE_NAME + " (\n" +
                "    " + COLUMN_FILE + " BLOB NOT NULL,\n" +
                "    " + COLUMN_NAME + " varchar(200) NOT NULL);";

        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        /*
         * We are doing nothing here
         * Just dropping and creating the table
         * */
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    /*
     * CREATE OPERATION
     * */

    boolean adddata(byte[] file,String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FILE,file);
        contentValues.put(COLUMN_NAME, name);
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }


    /*
     * READ OPERATION
     * */
    Cursor getAlldata() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    /*
     * checking data presence
     */
    Cursor getcount(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (cur != null) {
            cur.moveToFirst();
            return cur;
        }
        return cur;
    }


    /*
     * UPDATE OPERATION
     * */
    boolean updatefilesdata(String id, String name, String type,int size, String cdnpath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_TYPE, type);
        contentValues.put(COLUMN_sizeInBytes, size);
        contentValues.put(COLUMN_cdn_path, cdnpath);

        return db.update(TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }


    /*
     * DELETE OPERATION
     * */
    boolean deletefilesdata(String id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }
}

