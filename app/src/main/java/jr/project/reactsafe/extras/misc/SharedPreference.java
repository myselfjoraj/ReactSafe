package jr.project.reactsafe.extras.misc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Map;
import java.util.Set;

public class SharedPreference {

    private final SharedPreferences settings;

    //region Default Values for various types
    private int         intDefaultVal       = 0;
    private long        longDefaultVal      = 0;
    private float       floatDefaultVal     = 0;
    private boolean     boolDefaultVal      = false;
    private String      stringDefaultVal    = "";
    private Set<String> stringSetDefaultVal = null;
    //endregion

    //region Getters and Setters for default Values
    public int getIntDefaultVal() {
        return intDefaultVal;
    }

    public void setIntDefaultVal(int intDefaultVal) {
        this.intDefaultVal = intDefaultVal;
    }

    public long getLongDefaultVal() {
        return longDefaultVal;
    }

    public void setLongDefaultVal(long longDefaultVal) {
        this.longDefaultVal = longDefaultVal;
    }

    public float getFloatDefaultVal() {
        return floatDefaultVal;
    }

    public void setFloatDefaultVal(float floatDefaultVal) {
        this.floatDefaultVal = floatDefaultVal;
    }

    public boolean isBoolDefaultVal() {
        return boolDefaultVal;
    }

    public void setBoolDefaultVal(boolean boolDefaultVal) {
        this.boolDefaultVal = boolDefaultVal;
    }

    public String getStringDefaultVal() {
        return stringDefaultVal;
    }

    public void setStringDefaultVal(String stringDefaultVal) {
        this.stringDefaultVal = stringDefaultVal;
    }

    public Set<String> getStringSetDefaultVal() {
        return stringSetDefaultVal;
    }

    public void setStringSetDefaultVal(Set<String> stringSetDefaultVal) {
        this.stringSetDefaultVal = stringSetDefaultVal;
    }

    //region Constructors
    public SharedPreference(Context context) {
        String packageName = context.getPackageName();
        settings = context.getSharedPreferences(
                packageName + "_preferences", Context.MODE_PRIVATE);
    }

    // creates a new preference file with specified name
    public SharedPreference(Context context, String preferenceFileName) {
        this(context, preferenceFileName, 0);
    }

    // creates a new preference file with specified name and mode
    public SharedPreference(Context context, String preferenceFileName, int mode) {
        settings = context.getSharedPreferences(preferenceFileName, mode);
    }
    //endregion

    public void setUserTypeInPref(String type){
        settings.edit().putString("LoginType",type).commit();
    }
    public String getUserTypeInPref(){
        return settings.getString("LoginType", stringDefaultVal);
    }

    //region Put Methods
    public void putInt(String key, int val) {
        settings.edit().putInt(key, val).commit();
    }

    public void putString(String key, String val) {
        settings.edit().putString(key, val).commit();
    }

    public void putBoolean(String key, boolean val) {
        settings.edit().putBoolean(key, val).commit();
    }

    public void putFloat(String key, float val) {
        settings.edit().putFloat(key, val).commit();
    }

    public void putLong(String key, long val) {
        settings.edit().putLong(key, val).commit();
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void putStringSet(String key, Set<String> val) {
        settings.edit().putStringSet(key, val).commit();
    }
    //endregion

    //region Get Methods
    public Map<String, ?> getAll() {
        return settings.getAll();
    }

    public int getInt(String key, int defaultValue) {
        return settings.getInt(key, defaultValue);
    }

    public int getInt(String key) {
        return settings.getInt(key, intDefaultVal);
    }

    public String getString(String key, String defaultValue) {
        return settings.getString(key, defaultValue);
    }

    public String getString(String key) {
        return settings.getString(key, stringDefaultVal);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return settings.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return settings.getBoolean(key, boolDefaultVal);
    }

    public float getFloat(String key, float defaultValue) {
        return settings.getFloat(key, defaultValue);
    }

    public float getFloat(String key) {
        return settings.getFloat(key, floatDefaultVal);
    }

    public long getLong(String key, long defaultValue) {
        return settings.getLong(key, defaultValue);
    }

    public long getLong(String key) {
        return settings.getLong(key, longDefaultVal);
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return settings.getStringSet(key, defaultValue);
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key) {
        return settings.getStringSet(key, stringSetDefaultVal);
    }
    //endregion

    //region Listener registering and unregistering methods
    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.unregisterOnSharedPreferenceChangeListener(listener);
    }


}
