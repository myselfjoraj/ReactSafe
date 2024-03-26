package jr.project.reactsafe.extras.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

import jr.project.reactsafe.extras.model.RecentModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ReactSafeDb";
    private static final int DATABASE_VERSION = 1;
    private static final String LOG_NAME      = "DatabaseHelperClass";

    private Intent broadcastIntent;
    Context context;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(28)
    public DatabaseHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE FALL_DETECTS(ID INTEGER PRIMARY KEY AUTOINCREMENT,TIMESTAMP VARCHAR,LOCATION VARCHAR,LATITUDE VARCHAR,LONGITUDE VARCHAR,STATUS VARCHAR)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String insertFall(String timestamp,String location,String lat,String lng,String status){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("TIMESTAMP",timestamp);
        cv.put("LOCATION",location);
        cv.put("LATITUDE",lat);
        cv.put("LONGITUDE",lng);
        cv.put("STATUS",status);
        float r=database.insert("FALL_DETECTS",null,cv);
        if (r==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    /**
     * status val
     * 1 - detected fall
     * 2 - cancelled by user
     * 3 - cancelled by parent
     * **/
    public String updateAlert(String timestamp,String status){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATUS",status);
        float id=database.update("FALL_DETECTS",cv,"TIMESTAMP="+timestamp,null);
        if (id==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    public ArrayList<RecentModel> readRecentFalls(){
        ArrayList<RecentModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FALL_DETECTS ORDER BY ID DESC";
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){
            RecentModel model = new RecentModel(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );
            rec.add(model);
        }
        return rec;
    }

}
