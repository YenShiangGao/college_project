package com.ggfood.test.ggfood_api.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by s121 on 2017/10/29.
 */

public class FoodDataBase {
    public static final String db_name = "ggfood";

    private static FoodDataBase instance;
    private SQLiteDatabase db;
    private IceBoxTable iceBoxTable;

    public static FoodDataBase getDB(Context context){
        if(instance == null) {
            instance = new FoodDataBase(context);
        }
        return instance;
    }
    public void execSQL(String sql){
        db.execSQL(sql);
    }
    public Cursor rawQuery(String sql ,String[] selectionArgs){
        return db.rawQuery(sql,selectionArgs);
    }
    public Cursor rawQuery(String sq1){
        return db.rawQuery(sq1,null);
    }
    public long insert(String table,
                       String nullColumnHack,
                       ContentValues values){
        return db.insert(table,nullColumnHack,values);
    }
    public int delete(String table,
                      String whereClause,
                      String[] whereArgs){
        return db.delete(table,whereClause,whereArgs);
    }
    public int update(String table,
                      ContentValues values,
                      String whereClause,
                      String[] whereArgs){
        return db.update(table,values,whereClause,whereArgs);
    }
    private FoodDataBase(Context context){
        db = context.openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        SQLiteDatabase.CursorFactory factory;
        iceBoxTable = new IceBoxTable(this);
    }

    public IceBoxTable getIceBoxTable(){
        return iceBoxTable;
    }
}
