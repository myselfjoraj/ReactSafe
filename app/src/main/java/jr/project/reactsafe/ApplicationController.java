package jr.project.reactsafe;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;

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
    }

    public static Context getAppContext() {
        return context;
    }
}
