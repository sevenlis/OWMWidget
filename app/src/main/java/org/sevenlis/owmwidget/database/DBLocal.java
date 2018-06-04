package org.sevenlis.owmwidget.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sevenlis.owmwidget.classes.City;

import java.util.ArrayList;
import java.util.List;

public class DBLocal {
    private DBOpenHelper dbOpenHelper;
    private static final String TABLE_CITY = "city";
    
    public DBLocal(Context context) {
        this.dbOpenHelper = new DBOpenHelper(context);
    }
    
    public City getCity(int id) {
        City city = new City();
        
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CITY,null,"id = ?",new String[] {String.valueOf(id)},null,null,null,"1");
        if (cursor.moveToFirst()) {
            city = new City(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("country")),
                    cursor.getFloat(cursor.getColumnIndex("lat")),
                    cursor.getFloat(cursor.getColumnIndex("lon"))
            );
        }
        cursor.close();
        db.close();
        
        return city;
    }
    
    public Cursor getCitiesCursorFilter(String sNameFilter) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        if (sNameFilter == null || sNameFilter.isEmpty()) {
            return db.query(TABLE_CITY,null,"name IS NOT NULL",null,null,null,"name");
        } else {
            return db.query(TABLE_CITY,null,"name IS NOT NULL AND lower(name) LIKE ?",new String[] {String.format("%%%s%%", sNameFilter).toLowerCase()},null,null,"name");
        }
    }
    
    public void close() {
        if (dbOpenHelper != null) dbOpenHelper.close();
    }
}
