package com.summer.restclient.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

    private static final String DB_NAME="order.db";
    private static final int DB_VERSION=1;
    public DBhelper(Context context){super(context,DB_NAME,null,DB_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE IF NOT EXISTS orderForm("+
                "_ID INTEGER PRIMARY KEY,"+
                "total INTEGER NOT NULL,"+
                "address TEXT NOT NULL);";
        db.execSQL(sql);

        sql="CREATE TABLE IF NOT EXISTS foods("+
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "foodName TEXT NOT NULL,"+
                "foodCount INTEGER NOT NULL," +
                "orderID INTEGER NOT NULL);";

        db.execSQL(sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql="DROP TABLE IF EXISTS orderForm";
        db.execSQL(sql);
        sql="DROP TABLE IF EXISTS foods";
        db.execSQL(sql);
        onCreate(db);
    }
}
