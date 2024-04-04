package jr.project.reactsafe;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.StrictMode;

public class ApplicationController extends Application {

    public static Context context;
    public static MediaPlayer mediaPlayer;

    public static ProgressDialog progressDialog;

    public static void showPleaseWaitDialog(String msg) {
        progressDialog = new ProgressDialog(getAppContext());
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismissPleaseWaitDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static void setMediaPlayer(MediaPlayer mp){
        mediaPlayer = mp;
    }

    public static MediaPlayer getMediaPlayer(){
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }

    public static void releaseMediaPlayer(){
        if (mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static Context getAppContext() {
        return context;
    }
}
