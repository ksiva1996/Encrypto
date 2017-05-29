package com.leagueofshadows.encrypto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class Db extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "Encrypto.db";
        private static final String TABLE_NAME = "Files";
        private static final String COLUMN_NAME = "Name";
        private static final String COLUMN_ORIGINAL = "OriginalPath";
        private static final String COLUMN_SIZE = "Size";
        private static final String COLUMN_NEW = "NewPath";

        Db(Context context) {
            super(context, DATABASE_NAME , null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table Files " +
                            "(id integer primary key autoincrement , Name varchar not null, OriginalPath varchar not null , NewPath varchar not null , Size integer not null)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Files");
            onCreate(db);
        }

    int addFile(String name,String originalPath,String newPath,int size)
    {
        ContentValues con = new ContentValues();
        con.put(COLUMN_NAME,name);
        con.put(COLUMN_ORIGINAL,originalPath);
        con.put(COLUMN_NEW,newPath);
        con.put(COLUMN_SIZE,size);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME,null,con);
        Cursor cursor = getWritableDatabase().rawQuery(" SELECT MAX(id) FROM Files ",null);
        cursor.moveToFirst();
        int x = cursor.getInt(0);
        cursor.close();
        return x;
    }

    void deleteFile(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME," id = ? ", new String[]{Integer.toString(id)});
    }

    ArrayList<FileItem> getData()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM Files ",null);
        ArrayList<FileItem> FileItems = new ArrayList<>();
        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            int l = cursor.getCount();
            for (int i = 0; i < l; i++) {
                FileItem fileItem = new FileItem(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(1), cursor.getString(2), cursor.getString(3),cursor.getString(1));
                FileItems.add(fileItem);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return FileItems;
    }

}
