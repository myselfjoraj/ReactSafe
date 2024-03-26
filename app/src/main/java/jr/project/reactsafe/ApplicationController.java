package jr.project.reactsafe;

import android.app.Application;
import android.media.MediaPlayer;

public class ApplicationController extends Application {

    public static MediaPlayer mediaPlayer;

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
    }
}
