package com.ucy.ecu.gui.aid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "OBDAuto.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "Car";
    public static final String VALUES_ID = "ID";
    public static final String VALUES_NAME = "Name";
    public static final String VALUES_VALUE = "Value";
    public static final String VALUES_UNIT = "Unit";

    public static final String create ="create table Car " +
            "(ID integer primary key,Name text,Value real,Unit text)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS Car");
        onCreate(db);
    }

    public void deleteALL(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Car",null,null);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertValue(String name, Float Value, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        Cursor res =  readableDatabase.rawQuery( "select * from Car where name='"+name+"'", null );
        boolean exists=false;
        if(res.moveToFirst()){
            exists=true;
        }
        if(exists){
            updateVal(name,Value,unit);
        }else{
            contentValues.put(VALUES_NAME, name);
            contentValues.put(VALUES_VALUE, Value);
            contentValues.put(VALUES_UNIT,unit);
            db.insert(TABLE_NAME, null, contentValues);
        }
        res.close();
        return true;
    }

    public boolean updateVal (String name, Float value, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(VALUES_VALUE, value);
        contentValues.put(VALUES_UNIT, unit);
        db.update(TABLE_NAME, contentValues,  VALUES_NAME+"= ? ", new String[]{name});
        return true;
    }

    public ArrayList<String> getAllNames() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(VALUES_NAME)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public Double getValue(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car where Name ='"+name+"'", null );
        res.moveToFirst();

        double value = 0;
        if(!res.isAfterLast()){
            value = res.getDouble(res.getColumnIndex(VALUES_VALUE));
        }
        res.close();
        return value;
    }
    public ArrayList<Double> getAllVals() {
        ArrayList<Double> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getDouble(res.getColumnIndex(VALUES_VALUE)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<Integer> getIDs() {
        ArrayList<Integer> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getInt(res.getColumnIndex("ID")));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<String> getUnits(){
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(VALUES_UNIT)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public String getUnit(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Car where Name ='"+name+"'", null );
        res.moveToFirst();

        String unit = "";
        if(!res.isAfterLast()){
            unit = res.getString(res.getColumnIndex(VALUES_UNIT));
        }
        res.close();
        return unit;
    }
}
