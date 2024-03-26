package jr.project.reactsafe.parent;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;

public class ParentPreferenceHelper {

    SharedPreference mPref;
    Context context;


    public ParentPreferenceHelper(Context context) {
        this.context = context;
        mPref = new SharedPreference(context);
    }

    public void setLastGenCode(long code){
        mPref.putLong("lastGeneratedCode",code);
    }

    public long getLastGenCode(){
        return mPref.getLong("lastGeneratedCode",0);
    }

    public String getIsOnAccident(){
        return mPref.getString("IsOnAccident", null);
    }

    public void setIsOnAccident(String ts){
        mPref.putString("IsOnAccident",ts);
    }

    public void setPairedDevice(String s){
        mPref.putString("PairedDevice",s);
        //mPref.putString("PairedDevice",null);
    }


    public ArrayList<UserModel> getPairedDeviceDetails(){
        Gson gson = new Gson();
        String s = mPref.getString("PairedDevice");
        if (s!=null) {
            Type type = new TypeToken<ArrayList<UserModel>>() {
            }.getType();
            return gson.fromJson(s, type);
        }else {
            return null;
        }
    }

}