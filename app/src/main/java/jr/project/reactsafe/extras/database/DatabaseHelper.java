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

import com.google.gson.Gson;

import java.util.ArrayList;

import jr.project.reactsafe.extras.model.AlertModel;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.model.UserModel;

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

        db.execSQL("CREATE TABLE AMBULANCE_ACCEPTS(ID INTEGER PRIMARY KEY AUTOINCREMENT,TIMESTAMP VARCHAR,LATITUDE VARCHAR,LONGITUDE VARCHAR,STATUS VARCHAR," +
                "PATIENT VARCHAR,PARENT VARCHAR,HOSPITAL VARCHAR,POLICE VARCHAR)");

        db.execSQL("CREATE TABLE HOSPITAL_ACCEPTS(ID INTEGER PRIMARY KEY AUTOINCREMENT,TIMESTAMP VARCHAR,LATITUDE VARCHAR,LONGITUDE VARCHAR,STATUS VARCHAR," +
                "PATIENT VARCHAR,PARENT VARCHAR,AMBULANCE VARCHAR,POLICE VARCHAR)");

        db.execSQL("CREATE TABLE POLICE_ACCEPTS(ID INTEGER PRIMARY KEY AUTOINCREMENT,TIMESTAMP VARCHAR,LATITUDE VARCHAR,LONGITUDE VARCHAR,STATUS VARCHAR," +
                "PATIENT VARCHAR,PARENT VARCHAR,HOSPITAL VARCHAR,AMBULANCE VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // USER AND PARENT { FALL_DETECTS }
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

    // AMBULANCE { AMBULANCE_ACCEPTS }
    public String insertAmbulanceAccepts(String TIMESTAMP, String LATITUDE , String LONGITUDE , String STATUS,
                                         UserModel PATIENT,UserModel PARENT,UserModel HOSPITAL,UserModel POLICE ){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("TIMESTAMP",TIMESTAMP);
        cv.put("LATITUDE",LATITUDE);
        cv.put("LONGITUDE",LONGITUDE);
        cv.put("STATUS",STATUS);
        cv.put("PATIENT",new Gson().toJson(PATIENT));
        cv.put("PARENT",new Gson().toJson(PARENT));
        cv.put("HOSPITAL",new Gson().toJson(HOSPITAL));
        cv.put("POLICE",new Gson().toJson(POLICE));
        float r=database.insert("AMBULANCE_ACCEPTS",null,cv);
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
    public String updateAlertOnAmbulance(String timestamp,String status){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATUS",status);
        float id=database.update("AMBULANCE_ACCEPTS",cv,"TIMESTAMP="+timestamp,null);
        if (id==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    public ArrayList<AlertModel> readAmbulanceAccepts(){
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
