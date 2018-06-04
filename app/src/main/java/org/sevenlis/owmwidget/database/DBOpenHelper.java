package org.sevenlis.owmwidget.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBOpenHelper extends SQLiteOpenHelper {
    private Context ctx;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "cities.s3db";
    private static final String DB_PATH_SUFFIX = "/databases/";
    
    DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
        initDataBase();
    }
    
    private void copyDataBaseFromAsset() throws IOException{
        InputStream myInput = ctx.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName = getDatabasePath();
        
        // if the path doesn't exist first, create it
        File folder = new File(ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new IOException("Error making database folder.");
            }
        }
        
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
    
    private String getDatabasePath() {
        return ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }
    
    private void initDataBase() throws SQLException {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);
        
        if (!dbFile.exists()) {
            try {
                copyDataBaseFromAsset();
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
