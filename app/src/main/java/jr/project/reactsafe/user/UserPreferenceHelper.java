package jr.project.reactsafe.user;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;

public class UserPreferenceHelper {

    SharedPreference mPref;
    Context context;


    public UserPreferenceHelper(Context context){
        this.context = context;
        mPref = new SharedPreference(context);
    }

    public void setProfileName(String name){
        mPref.putString("myProfileName",name);
    }

    public void setProfileEmail(String email){
        mPref.putString("myProfileEmail", email);
    }

    public void setProfileImage(String url){
        mPref.putString("myProfileImage", url);
    }

    public void setProfileNumber(String number){
        mPref.putString("myProfileNumber", number);
    }

    public String getProfileName(){
        return mPref.getString("myProfileName",FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public String getProfileEmail(){
        return mPref.getString("myProfileEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public String getProfileImage(){
        return mPref.getString("myProfileImage", null);
    }

    public String getProfileNumber(){
        return mPref.getString("myProfileNumber", null);
    }

    public void setPairedDeviceDetails(String a){
        mPref.putString("PairedDevice",a);
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
