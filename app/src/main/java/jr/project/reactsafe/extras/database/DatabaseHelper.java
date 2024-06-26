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

import jr.project.reactsafe.extras.model.AcceptModel;
import jr.project.reactsafe.extras.model.AlertModel;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

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

        db.execSQL("CREATE TABLE FALL_DETECTS(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TIMESTAMP VARCHAR,LOCATION VARCHAR,LATITUDE VARCHAR,LONGITUDE VARCHAR,STATUS VARCHAR)");

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
        if (!CheckIsDataAlreadyInDBorNot("FALL_DETECTS","TIMESTAMP",timestamp)) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("TIMESTAMP", timestamp);
            cv.put("LOCATION", location);
            cv.put("LATITUDE", lat);
            cv.put("LONGITUDE", lng);
            cv.put("STATUS", status);
            float r = database.insert("FALL_DETECTS", null, cv);
            if (r == -1) {
                return "failed";
            } else {
                return "success";
            }
        }else {
            return "success";
        }
    }

    public boolean CheckIsDataAlreadyInDBorNot(String TableName,
                                                      String dbfield, String fieldValue) {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        String Query = "Select * from " + TableName + " where " + dbfield + " = " + fieldValue;
        Cursor cursor = sqldb.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
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
        ArrayList<String> ts = new ArrayList<>();

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
            String t = Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())+" "
                    +Extras.getTimeFromTimeStamp(model.getTimestamp());
            if (ts.isEmpty()){
                ts.add(t);
                rec.add(model);
            }else if (!ts.contains(t)){
                rec.add(model);
                ts.add(t);
            }

        }
        return rec;
    }

    // HOSPITAL { HOSPITAL_ACCEPTS }
    public String insertHospitalAccepts(String TIMESTAMP, String LATITUDE , String LONGITUDE , String STATUS,
                                         UserModel PATIENT,UserModel PARENT,UserModel AMBULANCE,UserModel POLICE){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("TIMESTAMP",TIMESTAMP);
        cv.put("LATITUDE",LATITUDE);
        cv.put("LONGITUDE",LONGITUDE);
        cv.put("STATUS",STATUS);
        cv.put("PATIENT",new Gson().toJson(PATIENT));
        cv.put("PARENT",new Gson().toJson(PARENT));
        cv.put("AMBULANCE",new Gson().toJson(AMBULANCE));
        cv.put("POLICE",new Gson().toJson(POLICE));
        float r=database.insert("HOSPITAL_ACCEPTS",null,cv);
        if (r==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    /**
     * status val
     * 1 - in progress
     * 2 - cancelled
     * 3 - completed
     * 4 - expired
     * **/
    public String updateAlertOnHospital(String timestamp,String status){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATUS",status);
        float id=database.update("HOSPITAL_ACCEPTS",cv,"TIMESTAMP="+timestamp,null);
        if (id==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    public ArrayList<AcceptModel> readHospitalAccepts(){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM HOSPITAL_ACCEPTS ORDER BY ID DESC";
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setAMBULANCE(returnModel(cursor.getString(7)));
            model.setPOLICE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    public ArrayList<AcceptModel> readHospitalAcceptsById(String id){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM HOSPITAL_ACCEPTS WHERE TIMESTAMP="+id;
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setAMBULANCE(returnModel(cursor.getString(7)));
            model.setPOLICE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    // AMBULANCE { AMBULANCE_ACCEPTS }
    public String insertAmbulanceAccepts(String TIMESTAMP, String LATITUDE , String LONGITUDE , String STATUS,
                                         UserModel PATIENT,UserModel PARENT,UserModel HOSPITAL,UserModel POLICE){
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
     * 1 - in progress
     * 2 - cancelled
     * 3 - completed
     * 4 - expired
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

    public ArrayList<AcceptModel> readAmbulanceAccepts(){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM AMBULANCE_ACCEPTS ORDER BY ID DESC";
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setHOSPITAL(returnModel(cursor.getString(7)));
            model.setPOLICE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    public ArrayList<AcceptModel> readAmbulanceAcceptsById(String id){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM AMBULANCE_ACCEPTS WHERE TIMESTAMP="+id;
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setHOSPITAL(returnModel(cursor.getString(7)));
            model.setPOLICE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    // POLICE { POLICE_ACCEPTS }
    public String insertPoliceAccepts(String TIMESTAMP, String LATITUDE , String LONGITUDE , String STATUS,
                                         UserModel PATIENT,UserModel PARENT,UserModel HOSPITAL,UserModel AMBULANCE){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("TIMESTAMP",TIMESTAMP);
        cv.put("LATITUDE",LATITUDE);
        cv.put("LONGITUDE",LONGITUDE);
        cv.put("STATUS",STATUS);
        cv.put("PATIENT",new Gson().toJson(PATIENT));
        cv.put("PARENT",new Gson().toJson(PARENT));
        cv.put("HOSPITAL",new Gson().toJson(HOSPITAL));
        cv.put("AMBULANCE",new Gson().toJson(AMBULANCE));
        float r=database.insert("POLICE_ACCEPTS",null,cv);
        if (r==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    /**
     * status val
     * 1 - in progress
     * 2 - cancelled
     * 3 - completed
     * 4 - expired
     * **/
    public String updateAlertOnPolice(String timestamp,String status){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATUS",status);
        float id=database.update("POLICE_ACCEPTS",cv,"TIMESTAMP="+timestamp,null);
        if (id==-1){
            return "failed";
        }else {
            return "success";
        }
    }

    public ArrayList<AcceptModel> readPoliceAccepts(){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM POLICE_ACCEPTS ORDER BY ID DESC";
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setHOSPITAL(returnModel(cursor.getString(7)));
            model.setAMBULANCE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    public ArrayList<AcceptModel> readPoliceAcceptsById(String id){
        ArrayList<AcceptModel> rec = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM POLICE_ACCEPTS WHERE TIMESTAMP="+id;
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){

            AcceptModel model = new AcceptModel();
            model.setTIMESTAMP(cursor.getString(1));
            model.setLATITUDE(cursor.getString(2));
            model.setLONGITUDE(cursor.getString(3));
            model.setSTATUS(cursor.getString(4));
            model.setPATIENT(returnModel(cursor.getString(5)));
            model.setPARENT(returnModel(cursor.getString(6)));
            model.setHOSPITAL(returnModel(cursor.getString(7)));
            model.setAMBULANCE(returnModel(cursor.getString(8)));

            rec.add(model);
        }
        return rec;
    }

    UserModel returnModel(String model){
        return new Gson().fromJson(model, UserModel.class);
    }

}
