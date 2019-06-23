package com.ggfood.test.ggfood_api.database;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by s121 on 2017/10/29.
 */

public class IceBoxTable {
    public static final String table_name = "ice_box";
    public static final String field_ID = "ID";
    public static final String field_NAME = "NAME";
    public static final String field_QUANTITY = "QUANTITY";
    public static final String field_EXPIRE = "EXPIRE";
    public static final String[] fieldNames = new String[]{field_ID,field_NAME,field_QUANTITY,field_EXPIRE};
    private FoodDataBase db;
    public IceBoxTable(FoodDataBase db){
        this.db = db;
        CreateTable();
    }

    private void CreateTable(){
        String createTable = "CREATE TABLE IF NOT EXISTS " +
                table_name + "("+
                field_ID+" TEXT," +
                field_NAME+" TEXT," +
                field_QUANTITY+" INTEGER," +
                field_EXPIRE+" DATE)";
        db.execSQL(createTable);
    }
    public Cursor selectAll(){
        return db.rawQuery("SELECT * FROM " + table_name,null);
    }
    public long insert(Item item){
        return db.insert(table_name, null, item.toContentValue());
    }
    public int deleteById(String id){
        return db.delete(table_name,field_ID + "=?",new String[]{id});
    }
    public int update(String targetId , Item item){
        return db.update(table_name,item.toContentValue(),field_ID+"=?",new String[]{targetId});
    }
    public int updateQuantity(String targetId , int quantity){
        ContentValues cv = new ContentValues(1);
        cv.put("quantity",quantity);
        return db.update(table_name,cv,field_ID+"=?",new String[]{targetId});
    }
    public String[] findNotExist(String names[]){
        ArrayList<String> result = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            Log.e("FIND","name[i]:"+names[i]);
            if(names[i] != null)
                result.add(names[i]);
        }
        Log.e("FFFFFFFFFFFFFFFFFUC","SELECT * FROM "+table_name+" WHERE "+field_NAME+" ("+ translateString(names) + ")");
        Cursor cursor = db.rawQuery("SELECT * FROM "+table_name+" WHERE "+field_NAME+" IN ("+ translateString(names) + ")");
        int nameIndex = cursor.getColumnIndex(field_NAME);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            //全數存到result
            do {
                Log.e("findNotExist", "RM : " + cursor.getString(nameIndex));
                result.remove(cursor.getString(nameIndex));
                Log.e("FIND","result[last]:"+result.get(result.size()-1));
            } while (cursor.moveToNext());
            //將names有但result沒有的傳回
        }
        for(int i=result.size()-1;i>=0;i--){
            if(result.get(i)==null)result.remove(i);
        }
        return result.toArray(new String[result.size()]);
    }
    private boolean has(String[] array,String str){
        for(int i=0;i<array.length;i++){
            if(array[i].equals(str))return true;
        }
        return false;
    }
    public static class Item implements Serializable {
        public String id;
        public String name;
        public String expire;
        public int quantity;

        //從Intent中讀取指定欄位並進行轉換
        public Item(Intent data){
            id = data.getStringExtra("ID");
            name = data.getStringExtra("NAME");
            expire = data.getStringExtra("EXPIRE");
            quantity = data.getIntExtra("QUANTITY", 0);
        }
        //從Cursor中讀取一筆並轉換
        public Item(Cursor c){
            id =c.getString(0);
            name = c.getString(1);
            expire = c.getString(3);
            quantity = c.getInt(2);
        }

        public Item(String id, String name, String expire, int quantity) {
            this.id = id;
            this.name = name;
            this.expire = expire;
            this.quantity = quantity;
        }

        public ContentValues toContentValue(){
            ContentValues cv = new ContentValues(4);
            if(id != null)
            cv.put("ID",id);
            if(name != null)
            cv.put("NAME",name);
            if(expire != null)
            cv.put("EXPIRE",expire);
            cv.put("QUANTITY", quantity);
            return cv;
        }

        @Override
        public String toString() {
            return String.format("id=%s,name=%s,deadline=%s,quantity=%d",id,name,expire, quantity);
        }

    }
    private String translateString(String[] args) {
        if (args.length < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            int len = args.length;
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append('\"');
            sb.append(args[0]);
            sb.append('\"');
            for (int i = 1; i < len; i++) {
                sb.append(",\"");
                sb.append(args[i]);
                sb.append('\"');
            }
            return sb.toString();
        }
    }

}
