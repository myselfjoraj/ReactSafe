package jr.project.reactsafe.extras.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jr.project.reactsafe.user.AlertLockScreenActivity;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("jr.project.reactsafe.FALL_DETECTED")) {
                Intent lockScreenIntent = new Intent(context, AlertLockScreenActivity.class);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(lockScreenIntent);
            }
        }
    }
}